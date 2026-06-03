package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.DebtorDao
import com.example.data.util.DataImportHandler
import com.example.domain.model.CallRecord
import com.example.domain.model.Debtor
import com.example.domain.repository.DebtorRepository
import com.example.domain.security.SecuritySettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DebtorRepository,
    private val securitySettingsStore: SecuritySettingsStore,
    private val debtorDao: DebtorDao,
    private val collectionDao: com.example.data.local.dao.CollectionDao
) : ViewModel() {

    fun getPromisesForDebtor(debtorId: String) = collectionDao.getAllPromisesToPay().map { list ->
        list.filter { it.debtorId == debtorId }
    }

    fun importStudentDataCollegeWise(csvContent: String, collegeName: String, onFinished: (Int, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val handler = DataImportHandler(debtorDao)
                val result = handler.parseAndValidateCsvText(csvContent)
                if (result.successfullyImported.isNotEmpty()) {
                    val count = handler.saveToLocalDatabase(result.successfullyImported, collegeName)
                    val errorString = if (result.errorLogs.isNotEmpty()) {
                        "Successfully imported $count rows. Some rows had errors:\n" + result.errorLogs.joinToString("\n")
                    } else {
                        null
                    }
                    onFinished(count, errorString)
                } else {
                    val errorString = if (result.errorLogs.isNotEmpty()) {
                        result.errorLogs.joinToString("\n")
                    } else {
                        "No student rows found to import. Please check format."
                    }
                    onFinished(0, errorString)
                }
            } catch (e: Exception) {
                onFinished(0, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun maskPhoneNumber(number: String): String {
        return securitySettingsStore.maskPhoneNumber(number)
    }

    val isNumberMaskingEnabled: StateFlow<Boolean> = securitySettingsStore.isNumberMaskingEnabled

    val telecallersList = securitySettingsStore.telecallersList

    // Expose flows from the domain repository
    val debtors = repository.getDebtors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCalls = repository.getRecentCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyCallProgress = repository.getDailyCallProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(42, 60))

    val dailyRecoveredAmount = repository.getDailyRecoveredAmount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120000.0)

    // Sim Card controls
    private val _currentSim = MutableStateFlow("SIM 1")
    val currentSim: StateFlow<String> = _currentSim.asStateFlow()

    // Simulated Active Call State
    private val _activeCallDebtor = MutableStateFlow<Debtor?>(null)
    val activeCallDebtor: StateFlow<Debtor?> = _activeCallDebtor.asStateFlow()

    private val _isDialing = MutableStateFlow(false)
    val isDialing: StateFlow<Boolean> = _isDialing.asStateFlow()

    // Profile detail & persistent edit state
    private val _selectedDebtor = MutableStateFlow<Debtor?>(null)
    val selectedDebtor: StateFlow<Debtor?> = _selectedDebtor.asStateFlow()

    // Call Disposition Flow State
    private val _dispositionDebtor = MutableStateFlow<Debtor?>(null)
    val dispositionDebtor: StateFlow<Debtor?> = _dispositionDebtor.asStateFlow()

    fun selectDispositionDebtor(debtor: Debtor?) {
        _dispositionDebtor.value = debtor
    }

    fun switchSim() {
        _currentSim.value = if (_currentSim.value == "SIM 1") "SIM 2" else "SIM 1"
    }

    fun initiateCall(debtor: Debtor) {
        viewModelScope.launch {
            _activeCallDebtor.value = debtor
            _isDialing.value = true
        }
    }

    fun endCallWithResult(status: String) {
        viewModelScope.launch {
            val debtor = _activeCallDebtor.value
            if (debtor != null) {
                repository.recordCall(
                    debtorName = debtor.name,
                    status = status,
                    simCard = _currentSim.value
                )
                _dispositionDebtor.value = debtor // Open the disposition screen for this debtor automatically!
            }
            _isDialing.value = false
            _activeCallDebtor.value = null
        }
    }

    fun cancelCall() {
        _isDialing.value = false
        _activeCallDebtor.value = null
    }

    // Debtor selection & edit routines
    fun selectDebtor(debtor: Debtor?) {
        _selectedDebtor.value = debtor
    }

    fun updateDebtorProfile(updated: Debtor) {
        viewModelScope.launch {
            repository.updateDebtor(updated)
            if (_selectedDebtor.value?.id == updated.id) {
                _selectedDebtor.value = updated
            }
        }
    }

    fun getCallLogsForDebtor(debtorId: String) = repository.getCallLogsForDebtor(debtorId)

    fun addCallLogEntry(debtorId: String, debtorName: String, outcome: String, notes: String) {
        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            repository.saveCallLog(
                debtorId = debtorId,
                debtorName = debtorName,
                date = currentDate,
                time = currentTime,
                outcome = outcome,
                notes = notes
            )
            val updatedDebtor = _selectedDebtor.value?.copy(lastContactDate = "0 Days Ago")
            if (updatedDebtor != null) {
                _selectedDebtor.value = updatedDebtor
            }
        }
    }
}
