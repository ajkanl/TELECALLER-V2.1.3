package com.example.presentation.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.PromiseToPayEntity
import com.example.domain.repository.FirebaseSyncRepository
import com.example.domain.security.SecuritySettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncSettingsStore @Inject constructor() {
    private val _wifiOnlyEnabled = MutableStateFlow(true)
    val wifiOnlyEnabled: StateFlow<Boolean> = _wifiOnlyEnabled.asStateFlow()

    // 0: Real-time Push, 1: Every 15 Minutes, 2: Hourly, 3: End of Shift Sync Only
    private val _syncFrequencyIndex = MutableStateFlow(1)
    val syncFrequencyIndex: StateFlow<Int> = _syncFrequencyIndex.asStateFlow()

    // Options: "24 Hours", "3 Days", "7 Days"
    private val _cacheExpiryThreshold = MutableStateFlow("3 Days")
    val cacheExpiryThreshold: StateFlow<String> = _cacheExpiryThreshold.asStateFlow()

    fun updateWifiOnlyEnabled(enabled: Boolean) {
        _wifiOnlyEnabled.value = enabled
    }

    fun updateSyncFrequencyIndex(index: Int) {
        _syncFrequencyIndex.value = index.coerceIn(0, 3)
    }

    fun updateCacheExpiryThreshold(threshold: String) {
        _cacheExpiryThreshold.value = threshold
    }
}

@OptIn(kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class DataSyncViewModel @Inject constructor(
    private val store: DataSyncSettingsStore,
    private val firebaseSyncRepository: FirebaseSyncRepository,
    private val database: AppDatabase,
    private val collectionDatabase: com.example.data.local.CollectionDatabase,
    private val securitySettingsStore: SecuritySettingsStore
) : ViewModel() {

    val syncEvents: SharedFlow<String> = firebaseSyncRepository.syncEvents

    val wifiOnlyEnabled: StateFlow<Boolean> = store.wifiOnlyEnabled
    val syncFrequencyIndex: StateFlow<Int> = store.syncFrequencyIndex
    val cacheExpiryThreshold: StateFlow<String> = store.cacheExpiryThreshold

    val allDebtorsList: StateFlow<List<DebtorEntity>> = collectionDatabase.collectionDao().getAllDebtorsList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCallLogsList: StateFlow<List<CallLogEntity>> = collectionDatabase.collectionDao().getAllCallLogsList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPromisesList: StateFlow<List<PromiseToPayEntity>> = collectionDatabase.collectionDao().getAllPromisesToPay()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val frequencyOptions = listOf(
        "Real-time Push",
        "Every 15 Minutes",
        "Hourly",
        "End of Shift Sync Only"
    )

    val cacheThresholdOptions = listOf(
        "24 Hours",
        "3 Days",
        "7 Days"
    )

    // Visual log feedback for Category-Wise cloud synchronization tracking
    private val _syncStatusLogs = MutableStateFlow<List<String>>(emptyList())
    val syncStatusLogs: StateFlow<List<String>> = _syncStatusLogs.asStateFlow()

    private val _isSyncingToCloud = MutableStateFlow(false)
    val isSyncingToCloud: StateFlow<Boolean> = _isSyncingToCloud.asStateFlow()

    init {
        // Collect real-time event updates emitted from FirebaseSyncRepository implementation
        viewModelScope.launch {
            syncEvents.collect { logMessage ->
                _syncStatusLogs.update { current -> current + logMessage }
            }
        }

        // Automatic background sync trigger!
        // We observe database flows and telecallers list, debounce them by 3 seconds after any change,
        // and trigger the sync automatically without manual action.
        viewModelScope.launch {
            try {
                combine(
                    collectionDatabase.collectionDao().getAllDebtorsList(),
                    collectionDatabase.collectionDao().getAllCallLogsList(),
                    collectionDatabase.collectionDao().getAllPromisesToPay(),
                    securitySettingsStore.telecallersList
                ) { debtors, calls, promises, telecallers ->
                    listOf(debtors.size, calls.size, promises.size, telecallers.hashCode())
                }
                .debounce(3000) // Wait 3 seconds of quiet time before uploading to avoid database write contention
                .collect { sizes ->
                    android.util.Log.d("DataSyncViewModel", "Database or telecallers change detected (Debtors: ${sizes[0]}, Calls: ${sizes[1]}, Promises: ${sizes[2]}, Callers Hash: ${sizes[3]}). Initiating automatic sync.")
                    triggerCategoryWisePushToCloudSilent()
                }
            } catch (e: Exception) {
                android.util.Log.e("DataSyncViewModel", "Error starting automatic cloud sync observer: ${e.message}", e)
            }
        }

        // Run an automatic initial push on launch to upload current data to Firestore
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            triggerCategoryWisePushToCloudSilent()
        }
    }

    fun clearLogHistory() {
        _syncStatusLogs.value = emptyList()
    }

    fun triggerCategoryWisePushToCloudSilent() {
        if (_isSyncingToCloud.value) return
        _isSyncingToCloud.value = true

        viewModelScope.launch {
            try {
                val collectionDao = collectionDatabase.collectionDao()
                val debtorsList = collectionDao.getDebtorsDirect()
                val callLogsList = collectionDao.getCallLogsDirect()
                val promisesList = collectionDao.getPromisesToPayDirect()

                val agentsList = securitySettingsStore.telecallersList.value
                val securityPolicyMap = mapOf(
                    "isNumberMaskingEnabled" to securitySettingsStore.isNumberMaskingEnabled.value,
                    "isHardwareBindingEnabled" to securitySettingsStore.isHardwareBindingEnabled.value,
                    "isScreenshotBlockEnabled" to securitySettingsStore.isScreenshotBlockEnabled.value,
                    "themeMode" to securitySettingsStore.themeMode.value
                )
                val syncCriteriaMap = mapOf(
                    "wifiOnlyEnabled" to store.wifiOnlyEnabled.value,
                    "syncFrequencyIndex" to store.syncFrequencyIndex.value,
                    "cacheExpiryThreshold" to store.cacheExpiryThreshold.value
                )

                firebaseSyncRepository.pushAllDataToFirestore(
                    debtors = debtorsList,
                    callLogs = callLogsList,
                    promises = promisesList,
                    agents = agentsList,
                    securitySettings = securityPolicyMap,
                    syncSettings = syncCriteriaMap
                )
            } catch (e: Exception) {
                _syncStatusLogs.update { current -> current + "ERROR: Background auto sync halted -> ${e.localizedMessage}" }
            } finally {
                _isSyncingToCloud.value = false
            }
        }
    }

    fun triggerCategoryWisePushToCloud() {
        if (_isSyncingToCloud.value) return
        _isSyncingToCloud.value = true
        _syncStatusLogs.value = emptyList()

        viewModelScope.launch {
            try {
                // Fetch local records from separate categories
                val collectionDao = collectionDatabase.collectionDao()
                val debtorsList = collectionDao.getDebtorsDirect()
                val callLogsList = collectionDao.getCallLogsDirect()
                val promisesList = collectionDao.getPromisesToPayDirect()

                // Fetch security profiles and sync settings
                val agentsList = securitySettingsStore.telecallersList.value
                val securityPolicyMap = mapOf(
                    "isNumberMaskingEnabled" to securitySettingsStore.isNumberMaskingEnabled.value,
                    "isHardwareBindingEnabled" to securitySettingsStore.isHardwareBindingEnabled.value,
                    "isScreenshotBlockEnabled" to securitySettingsStore.isScreenshotBlockEnabled.value,
                    "themeMode" to securitySettingsStore.themeMode.value
                )
                val syncCriteriaMap = mapOf(
                    "wifiOnlyEnabled" to store.wifiOnlyEnabled.value,
                    "syncFrequencyIndex" to store.syncFrequencyIndex.value,
                    "cacheExpiryThreshold" to store.cacheExpiryThreshold.value
                )

                // Execute the repository push to Firebase Firestore categories-wise
                firebaseSyncRepository.pushAllDataToFirestore(
                    debtors = debtorsList,
                    callLogs = callLogsList,
                    promises = promisesList,
                    agents = agentsList,
                    securitySettings = securityPolicyMap,
                    syncSettings = syncCriteriaMap
                )
            } catch (e: Exception) {
                _syncStatusLogs.update { current -> current + "ERROR: General failure in local queries -> ${e.localizedMessage}" }
            } finally {
                _isSyncingToCloud.value = false
            }
        }
    }

    fun triggerCategoryWisePullFromCloud() {
        if (_isSyncingToCloud.value) return
        _isSyncingToCloud.value = true
        _syncStatusLogs.value = emptyList()

        viewModelScope.launch {
            try {
                firebaseSyncRepository.pullDataFromFirestore(securitySettingsStore)
            } catch (e: Exception) {
                _syncStatusLogs.update { current -> current + "ERROR: General failure in cloud pull -> ${e.localizedMessage}" }
            } finally {
                _isSyncingToCloud.value = false
            }
        }
    }

    fun wipeAllLocalAndCloudDataDirect() {
        if (_isSyncingToCloud.value) return
        _isSyncingToCloud.value = true
        _syncStatusLogs.value = emptyList()

        viewModelScope.launch {
            try {
                _syncStatusLogs.update { current -> current + "Initializing deep wipe sequence..." }
                
                // Clear all local database tables in Room
                kotlinx.coroutines.withContext(Dispatchers.IO) {
                    database.clearAllTables()
                    collectionDatabase.clearAllTables()
                }
                _syncStatusLogs.update { current -> current + "SUCCESS: All localized databases cleared of records." }
                _syncStatusLogs.update { current -> current + "Syncing clean, empty slate to Cloud Firestore collections..." }

                val agentsList = securitySettingsStore.telecallersList.value
                val securityPolicyMap = mapOf(
                    "isNumberMaskingEnabled" to securitySettingsStore.isNumberMaskingEnabled.value,
                    "isHardwareBindingEnabled" to securitySettingsStore.isHardwareBindingEnabled.value,
                    "isScreenshotBlockEnabled" to securitySettingsStore.isScreenshotBlockEnabled.value,
                    "themeMode" to securitySettingsStore.themeMode.value
                )
                val syncCriteriaMap = mapOf(
                    "wifiOnlyEnabled" to store.wifiOnlyEnabled.value,
                    "syncFrequencyIndex" to store.syncFrequencyIndex.value,
                    "cacheExpiryThreshold" to store.cacheExpiryThreshold.value
                )

                firebaseSyncRepository.pushAllDataToFirestore(
                    debtors = emptyList(),
                    callLogs = emptyList(),
                    promises = emptyList(),
                    agents = agentsList,
                    securitySettings = securityPolicyMap,
                    syncSettings = syncCriteriaMap
                )
                
                _syncStatusLogs.update { current -> current + "SUCCESS: Database Reset successfully synced to Cloud!" }
            } catch (e: Exception) {
                _syncStatusLogs.update { current -> current + "ERROR: Failed during full database purge -> ${e.localizedMessage}" }
            } finally {
                _isSyncingToCloud.value = false
            }
        }
    }

    fun updateWifiOnlyEnabled(enabled: Boolean) {
        store.updateWifiOnlyEnabled(enabled)
    }

    fun updateSyncFrequencyIndex(index: Int) {
        store.updateSyncFrequencyIndex(index)
    }

    fun updateCacheExpiryThreshold(threshold: String) {
        store.updateCacheExpiryThreshold(threshold)
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    fun exportTypeToCsvFile(context: Context, dataType: String): Uri? {
        return try {
            val content = when (dataType) {
                "STUDENTS" -> {
                    val list = allDebtorsList.value
                    val sb = java.lang.StringBuilder()
                    sb.append("\uFEFF") // UTF-8 BOM
                    sb.append("Roll No/Account Number,Student Name,Primary Contact,Alt Contact,Outstanding Dues,Base Fee,DPD Bucket,Current Status,College,Address,Remarks\n")
                    for (d in list) {
                        sb.append("\"${escapeCsv(d.id)}\",\"${escapeCsv(d.name)}\",\"${escapeCsv(d.phoneNumber)}\",\"${escapeCsv(d.alternativeNumber ?: "")}\",${d.totalOverdueAmount},${d.principalAmount},\"${escapeCsv(d.dpdBucket)}\",\"${escapeCsv(d.currentStatus)}\",\"${escapeCsv(d.college)}\",\"${escapeCsv(d.address)}\",\"${escapeCsv(d.remarks)}\"\n")
                    }
                    sb.toString()
                }
                "CALL_LOGS" -> {
                    val list = allCallLogsList.value
                    val sb = java.lang.StringBuilder()
                    sb.append("\uFEFF") // UTF-8 BOM
                    sb.append("Call ID,Student ID,Timestamp,Duration (Secs),Type,Outcome,Agent Notes,Date,Time\n")
                    for (l in list) {
                        sb.append("${l.callId},\"${escapeCsv(l.debtorId)}\",${l.callTimestamp},${l.durationSeconds},\"${escapeCsv(l.callType)}\",\"${escapeCsv(l.callDisposition)}\",\"${escapeCsv(l.agentNotes ?: "")}\",\"${escapeCsv(l.date)}\",\"${escapeCsv(l.time)}\"\n")
                    }
                    sb.toString()
                }
                "PAYMENTS" -> {
                    val list = allPromisesList.value
                    val sb = java.lang.StringBuilder()
                    sb.append("\uFEFF") // UTF-8 BOM
                    sb.append("PTP ID,Student ID,Created Timestamp,Target Date,Amount,PTP Status\n")
                    for (tp in list) {
                        sb.append("${tp.ptpId},\"${escapeCsv(tp.debtorId)}\",${tp.ptpCreationTimestamp},${tp.promisedPaymentDate},${tp.promisedAmount},\"${escapeCsv(tp.ptpStatus)}\"\n")
                    }
                    sb.toString()
                }
                else -> ""
            }

            val fileName = when (dataType) {
                "STUDENTS" -> "student_database_export.csv"
                "CALL_LOGS" -> "call_logs_export.csv"
                else -> "payment_commitments_export.csv"
            }

            val file = File(context.cacheDir, fileName)
            val writer = FileWriter(file)
            writer.write(content)
            writer.flush()
            writer.close()

            FileProvider.getUriForFile(context, "com.example.fileprovider", file)
        } catch (e: Exception) {
            android.util.Log.e("DataSyncViewModel", "Failed to export data: ${e.message}", e)
            null
        }
    }

    fun bulkUploadStudents(rawCsvText: String): Result<Int> {
        val lines = rawCsvText.split('\n')
        val debtors = mutableListOf<DebtorEntity>()
        var headers: List<String>? = null
        
        return try {
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) continue
                
                val tokens = trimmedLine.split(",").map { it.trim().removeSurrounding("\"") }
                if (headers == null) {
                    headers = tokens.map { it.lowercase() }
                    continue
                }
                
                if (tokens.size < 3) continue
                
                val id = tokens.getOrNull(0) ?: java.util.UUID.randomUUID().toString()
                val name = tokens.getOrNull(1) ?: "Unknown Student"
                val phone = tokens.getOrNull(2) ?: "00000"
                val altPhone = tokens.getOrNull(3)?.ifEmpty { null }
                val overdueStr = tokens.getOrNull(4) ?: "0.0"
                val overdue = overdueStr.toDoubleOrNull() ?: 0.0
                val principalStr = tokens.getOrNull(5) ?: overdueStr
                val principal = principalStr.toDoubleOrNull() ?: overdue
                val bucket = tokens.getOrNull(6)?.ifEmpty { "1-30" } ?: "1-30"
                val college = tokens.getOrNull(7)?.ifEmpty { "Engineering" } ?: "Engineering"
                val address = tokens.getOrNull(8)?.ifEmpty { "Default Address" } ?: "Default Address"
                val remarks = tokens.getOrNull(9)?.ifEmpty { "Bulk uploaded student" } ?: "Bulk uploaded student"

                val debtor = DebtorEntity(
                    id = id,
                    name = name,
                    phoneNumber = phone,
                    alternativeNumber = altPhone,
                    totalOverdueAmount = overdue,
                    principalAmount = principal,
                    dpdBucket = bucket,
                    allocationDate = System.currentTimeMillis(),
                    currentStatus = "PENDING",
                    contactNumber = phone,
                    address = address,
                    outstandingAmount = overdue,
                    lastContactDate = "0 Days Ago",
                    customerSegment = "Standard",
                    college = college,
                    remarks = remarks
                )
                debtors.add(debtor)
            }

            if (debtors.isEmpty()) {
                return Result.failure(Exception("No valid student records found to upload."))
            }

            viewModelScope.launch(Dispatchers.IO) {
                database.collectionDao().insertOrUpdateDebtors(debtors)
                triggerCategoryWisePushToCloudSilent()
            }
            _syncStatusLogs.update { current ->
                current + "SUCCESS: Parsed & bulk-uploaded ${debtors.size} student records!"
            }
            Result.success(debtors.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
