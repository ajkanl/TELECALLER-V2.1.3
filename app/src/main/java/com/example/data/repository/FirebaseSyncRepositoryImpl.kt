package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.PromiseToPayEntity
import com.example.domain.security.TelecallerAgent
import com.example.domain.repository.FirebaseSyncRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : FirebaseSyncRepository {

    private val _syncEvents = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val syncEvents: SharedFlow<String> = _syncEvents.asSharedFlow()

    override suspend fun syncBulkDataFromCloud(updatedList: List<DebtorEntity>) {
        if (updatedList.isEmpty()) {
            _syncEvents.emit("Sync Complete: 0 Records Updated Localized")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                // Perform the batch insert inside a single Room Transaction
                database.withTransaction {
                    val debtorDao = database.debtorDao()
                    debtorDao.insertDebtors(updatedList)
                }

                // Fire the requested broadcast / completion event
                val message = "Sync Complete: ${updatedList.size} Records Updated Localized"
                _syncEvents.emit(message)
            } catch (e: Exception) {
                _syncEvents.emit("Sync Failed: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun pushAllDataToFirestore(
        debtors: List<DebtorEntity>,
        callLogs: List<CallLogEntity>,
        promises: List<PromiseToPayEntity>,
        agents: List<TelecallerAgent>,
        securitySettings: Map<String, Any>,
        syncSettings: Map<String, Any>
    ): Boolean = withContext(Dispatchers.IO) {
        var overallSuccess = true
        _syncEvents.emit("Initializing Cloud Sync Sequence...")
        val db = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            _syncEvents.emit("FAILURE: Could not initialize Firebase Firestore -> ${e.localizedMessage}")
            return@withContext false
        }

        // 1. Sync Debtors Collection (Students)
        try {
            _syncEvents.emit("Category 1/7: Uploading Student Debtors portfolio (${debtors.size} items)...")
            val debtorCollection = db.collection("debtors")
            if (debtors.isNotEmpty()) {
                val batchChunks = debtors.chunked(500)
                for (chunk in batchChunks) {
                    val batch = db.batch()
                    for (debtor in chunk) {
                        val docRef = debtorCollection.document(debtor.id)
                        val data = mapOf(
                            "id" to debtor.id,
                            "name" to debtor.name,
                            "phoneNumber" to debtor.phoneNumber,
                            "alternativeNumber" to debtor.alternativeNumber,
                            "totalOverdueAmount" to debtor.totalOverdueAmount,
                            "principalAmount" to debtor.principalAmount,
                            "dpdBucket" to debtor.dpdBucket,
                            "allocationDate" to debtor.allocationDate,
                            "currentStatus" to debtor.currentStatus,
                            "contactNumber" to debtor.contactNumber,
                            "address" to debtor.address,
                            "outstandingAmount" to debtor.outstandingAmount,
                            "lastContactDate" to debtor.lastContactDate,
                            "customerSegment" to debtor.customerSegment
                        )
                        batch.set(docRef, data)
                    }
                    batch.commit().await()
                }
            } else {
                debtorCollection.document("_welcome_placeholder_").set(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting localized client portfolios.",
                        "status" to "ACTIVE",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
            _syncEvents.emit("Category 1/7 Successful: Student portfolio synced.")
        } catch (e: Exception) {
            overallSuccess = false
            _syncEvents.emit("Category 1/7 Failed: ${e.localizedMessage}")
            android.util.Log.e("FirebaseSync", "Category 1 sync error", e)
        }

        // 2. Sync Call Logs Collection
        try {
            _syncEvents.emit("Category 2/7: Uploading Communication Logs (${callLogs.size} items)...")
            val callLogsCollection = db.collection("call_logs")
            if (callLogs.isNotEmpty()) {
                val batchChunks = callLogs.chunked(500)
                for (chunk in batchChunks) {
                    val batch = db.batch()
                    for (log in chunk) {
                        val docRef = callLogsCollection.document(log.id)
                        val data = mapOf(
                            "id" to log.id,
                            "callId" to log.callId,
                            "debtorId" to log.debtorId,
                            "callTimestamp" to log.callTimestamp,
                            "durationSeconds" to log.durationSeconds,
                            "callType" to log.callType,
                            "callDisposition" to log.callDisposition,
                            "agentNotes" to log.agentNotes,
                            "recordingFilePath" to log.recordingFilePath,
                            "date" to log.date,
                            "time" to log.time,
                            "outcome" to log.outcome,
                            "notes" to log.notes
                        )
                        batch.set(docRef, data)
                    }
                    batch.commit().await()
                }
            } else {
                callLogsCollection.document("_welcome_placeholder_").set(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting communication and disposition recordings.",
                        "status" to "EMPTY",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
            _syncEvents.emit("Category 2/7 Successful: Call records synced.")
        } catch (e: Exception) {
            overallSuccess = false
            _syncEvents.emit("Category 2/7 Failed: ${e.localizedMessage}")
            android.util.Log.e("FirebaseSync", "Category 2 sync error", e)
        }

        // 3. Sync Promises to Pay Collection
        try {
            _syncEvents.emit("Category 3/7: Uploading Payment Commitments / PTP (${promises.size} items)...")
            val ptpCollection = db.collection("promises_to_pay")
            if (promises.isNotEmpty()) {
                val batchChunks = promises.chunked(500)
                for (chunk in batchChunks) {
                    val batch = db.batch()
                    for (ptp in chunk) {
                        val docRef = ptpCollection.document(ptp.ptpId.toString())
                        val data = mapOf(
                            "ptpId" to ptp.ptpId,
                            "debtorId" to ptp.debtorId,
                            "ptpCreationTimestamp" to ptp.ptpCreationTimestamp,
                            "promisedPaymentDate" to ptp.promisedPaymentDate,
                            "promisedAmount" to ptp.promisedAmount,
                            "ptpStatus" to ptp.ptpStatus
                        )
                        batch.set(docRef, data)
                    }
                    batch.commit().await()
                }
            } else {
                ptpCollection.document("_welcome_placeholder_").set(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting payment commitments.",
                        "status" to "EMPTY",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
            _syncEvents.emit("Category 3/7 Successful: PTP commitments backed up.")
        } catch (e: Exception) {
            overallSuccess = false
            _syncEvents.emit("Category 3/7 Failed: ${e.localizedMessage}")
            android.util.Log.e("FirebaseSync", "Category 3 sync error", e)
        }

        // 4. Sync Telecaller Profiles Collection
        try {
            _syncEvents.emit("Category 4/7: Backing up Telecaller Profile Matrix (${agents.size} items)...")
            val telecallersCollection = db.collection("telecallers")
            if (agents.isNotEmpty()) {
                val batchChunks = agents.chunked(500)
                for (chunk in batchChunks) {
                    val batch = db.batch()
                    for (agent in chunk) {
                        val docRef = telecallersCollection.document(agent.id)
                        val data = mapOf(
                            "id" to agent.id,
                            "name" to agent.name,
                            "isOnline" to agent.isOnline,
                            "callsDialed" to agent.callsDialed,
                            "talkTimeMinutes" to agent.talkTimeMinutes,
                            "ptpsSecured" to agent.ptpsSecured,
                            "permissions" to mapOf(
                                "callInitiation" to agent.permissions.callInitiation,
                                "canSeeFullNumbers" to agent.permissions.canSeeFullNumbers,
                                "canPerformPurge" to agent.permissions.canPerformPurge,
                                "canRecordAudio" to agent.permissions.canRecordAudio,
                                "isAdmin" to agent.permissions.isAdmin
                              )
                        )
                        batch.set(docRef, data)
                    }
                    batch.commit().await()
                }
            } else {
                telecallersCollection.document("_welcome_placeholder_").set(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting caller profile allocation matrix tables.",
                        "status" to "EMPTY",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
            _syncEvents.emit("Category 4/7 Successful: Telecaller matrices backed up.")
        } catch (e: Exception) {
            overallSuccess = false
            _syncEvents.emit("Category 4/7 Failed: ${e.localizedMessage}")
            android.util.Log.e("FirebaseSync", "Category 4 sync error", e)
        }

        // 5. Sync Security and Compliance Policies Document
        try {
            _syncEvents.emit("Category 5/7: Securing compliance security rules document...")
            db.collection("security_settings_audit")
                .document("current_policies")
                .set(securitySettings)
                .await()
            _syncEvents.emit("Category 5/7 Successful: Security and compliance standards updated.")
        } catch (e: Exception) {
            overallSuccess = false
            _syncEvents.emit("Category 5/7 Failed: ${e.localizedMessage}")
            android.util.Log.e("FirebaseSync", "Category 5 sync error", e)
        }

        // 6. Sync Sync / Performance Configuration Document
        try {
            _syncEvents.emit("Category 6/7: Archiving operation sync config criteria...")
            db.collection("sync_settings_audit")
                .document("current_criteria")
                .set(syncSettings)
                .await()
            _syncEvents.emit("Category 6/7 Successful: Optimization rules locked.")
        } catch (e: Exception) {
            overallSuccess = false
            _syncEvents.emit("Category 6/7 Failed: ${e.localizedMessage}")
            android.util.Log.e("FirebaseSync", "Category 6 sync error", e)
        }

        // 7. Sync Telecaller Remarks Wise Call History Collection (Grouping: Telecaller & Remarks Wise)
        val groupedLogs = callLogs.groupBy { Pair(it.agentId, it.callDisposition) }
        try {
            _syncEvents.emit("Category 7/7: Organizing Telecaller and Remarks-wise Grouped Sync...")
            val teleRemarksCollection = db.collection("telecaller_remarks_wise")

            if (groupedLogs.isNotEmpty()) {
                _syncEvents.emit("Category 7/7: Uploading ${groupedLogs.size} distinct Telecaller-Remarks pairings...")
                for ((key, logsForGroup) in groupedLogs) {
                    val currentAgentId = key.first
                    val currentOutcome = key.second
                    val rawSlug = currentOutcome.replace("/", "_").replace(" ", "_").trim()
                    val docId = "${currentAgentId}_${rawSlug}"

                    val matchingAgent = agents.firstOrNull { it.id == currentAgentId }
                    val currentAgentName = matchingAgent?.name ?: logsForGroup.firstOrNull()?.agentName ?: "Rajesh Kumar"

                    val parentDocRef = teleRemarksCollection.document(docId)
                    val parentData = mapOf(
                        "telecallerId" to currentAgentId,
                        "telecallerName" to currentAgentName,
                        "remarkOutcome" to currentOutcome,
                        "callCount" to logsForGroup.size,
                        "lastUpdated" to (logsForGroup.maxOfOrNull { it.callTimestamp } ?: System.currentTimeMillis())
                    )
                    parentDocRef.set(parentData, com.google.firebase.firestore.SetOptions.merge()).await()

                    val subCollectionRef = parentDocRef.collection("calls")
                    val tasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
                    for (log in logsForGroup) {
                        val debtorName = debtors.firstOrNull { it.id == log.debtorId }?.name ?: "Unknown"
                        val callDetailData = mapOf(
                            "id" to log.id,
                            "debtorId" to log.debtorId,
                            "debtorName" to debtorName,
                            "timestamp" to log.callTimestamp,
                            "date" to log.date,
                            "time" to log.time,
                            "outcome" to log.callDisposition,
                            "remarks" to (log.agentNotes ?: log.notes),
                            "callType" to log.callType,
                            "agentId" to currentAgentId,
                            "agentName" to currentAgentName
                        )
                        tasks.add(subCollectionRef.document(log.id).set(callDetailData))
                    }
                    if (tasks.isNotEmpty()) {
                        Tasks.whenAllComplete(tasks).await()
                    }
                }
                _syncEvents.emit("Category 7/7 Successful: Telecaller-Outcome groupings synced beautifully.")
            } else {
                teleRemarksCollection.document("_welcome_placeholder_").set(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting telecaller and remarks-wise grouped entries.",
                        "status" to "EMPTY",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
                _syncEvents.emit("Category 7/7 (Empty Placeholder Added): Telecaller-Outcome groupings initialized.")
            }
        } catch (e: Exception) {
            overallSuccess = false
            _syncEvents.emit("Category 7/7 Failed: ${e.localizedMessage}")
            android.util.Log.e("FirebaseSync", "Category 7 sync error", e)
        }

        // --- Sync to Realtime Database (RTDB) as well ---
        try {
            _syncEvents.emit("RTDB: Initializing Realtime Database mirror copy synchronizer...")
            val rtdb = com.example.data.util.FirebaseDatabaseConnector.getInstance()
            if (rtdb == null) {
                _syncEvents.emit("RTDB Warning: Realtime Database module is unconfigured. Mirror check skipped.")
            } else {
                val rtdbRef = rtdb.reference

                // Helper function to sanitize RTDB path keys to avoid syntax crashes
                fun sanitizeKey(key: String): String {
                return key.replace(".", "_")
                    .replace("$", "_")
                    .replace("#", "_")
                    .replace("[", "_")
                    .replace("]", "_")
                    .replace("/", "_")
            }

            // 1. RTDB Debtors
            _syncEvents.emit("RTDB Category 1/7: Syncing student debtors...")
            val rtdbDebtors = mutableMapOf<String, Any>()
            for (debtor in debtors) {
                val cleanId = sanitizeKey(debtor.id)
                rtdbDebtors[cleanId] = mapOf(
                    "id" to debtor.id,
                    "name" to debtor.name,
                    "phoneNumber" to debtor.phoneNumber,
                    "alternativeNumber" to debtor.alternativeNumber,
                    "totalOverdueAmount" to debtor.totalOverdueAmount,
                    "principalAmount" to debtor.principalAmount,
                    "dpdBucket" to debtor.dpdBucket,
                    "allocationDate" to debtor.allocationDate,
                    "currentStatus" to debtor.currentStatus,
                    "contactNumber" to debtor.contactNumber,
                    "address" to debtor.address,
                    "outstandingAmount" to debtor.outstandingAmount,
                    "lastContactDate" to debtor.lastContactDate,
                    "customerSegment" to debtor.customerSegment
                )
            }
            if (rtdbDebtors.isNotEmpty()) {
                rtdbRef.child("debtors").setValue(rtdbDebtors).await()
            } else {
                rtdbRef.child("debtors").child("_welcome_placeholder_").setValue(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting localized client portfolios.",
                        "status" to "ACTIVE",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }

            // 2. RTDB Call Logs
            _syncEvents.emit("RTDB Category 2/7: Syncing communication logs...")
            val rtdbCallLogs = mutableMapOf<String, Any>()
            for (log in callLogs) {
                val cleanId = sanitizeKey(log.id)
                rtdbCallLogs[cleanId] = mapOf(
                    "id" to log.id,
                    "callId" to log.callId,
                    "debtorId" to log.debtorId,
                    "callTimestamp" to log.callTimestamp,
                    "durationSeconds" to log.durationSeconds,
                    "callType" to log.callType,
                    "callDisposition" to log.callDisposition,
                    "agentNotes" to log.agentNotes,
                    "recordingFilePath" to log.recordingFilePath,
                    "date" to log.date,
                    "time" to log.time,
                    "outcome" to log.outcome,
                    "notes" to log.notes
                )
            }
            if (rtdbCallLogs.isNotEmpty()) {
                rtdbRef.child("call_logs").setValue(rtdbCallLogs).await()
            } else {
                rtdbRef.child("call_logs").child("_welcome_placeholder_").setValue(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting communication and disposition recordings.",
                        "status" to "EMPTY",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }

            // 3. RTDB Promises to Pay
            _syncEvents.emit("RTDB Category 3/7: Syncing payment promises...")
            val rtdbPromises = mutableMapOf<String, Any>()
            for (ptp in promises) {
                val cleanId = sanitizeKey(ptp.ptpId.toString())
                rtdbPromises[cleanId] = mapOf(
                    "ptpId" to ptp.ptpId,
                    "debtorId" to ptp.debtorId,
                    "ptpCreationTimestamp" to ptp.ptpCreationTimestamp,
                    "promisedPaymentDate" to ptp.promisedPaymentDate,
                    "promisedAmount" to ptp.promisedAmount,
                    "ptpStatus" to ptp.ptpStatus
                )
            }
            if (rtdbPromises.isNotEmpty()) {
                rtdbRef.child("promises_to_pay").setValue(rtdbPromises).await()
            } else {
                rtdbRef.child("promises_to_pay").child("_welcome_placeholder_").setValue(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting payment commitments.",
                        "status" to "EMPTY",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }

            // 4. RTDB Telecallers
            _syncEvents.emit("RTDB Category 4/7: Syncing telecaller matrix...")
            val rtdbAgents = mutableMapOf<String, Any>()
            for (agent in agents) {
                val cleanId = sanitizeKey(agent.id)
                rtdbAgents[cleanId] = mapOf(
                    "id" to agent.id,
                    "name" to agent.name,
                    "isOnline" to agent.isOnline,
                    "callsDialed" to agent.callsDialed,
                    "talkTimeMinutes" to agent.talkTimeMinutes,
                    "ptpsSecured" to agent.ptpsSecured,
                    "permissions" to mapOf(
                        "callInitiation" to agent.permissions.callInitiation,
                        "canSeeFullNumbers" to agent.permissions.canSeeFullNumbers,
                        "canPerformPurge" to agent.permissions.canPerformPurge,
                        "canRecordAudio" to agent.permissions.canRecordAudio,
                        "isAdmin" to agent.permissions.isAdmin
                    )
                )
            }
            if (rtdbAgents.isNotEmpty()) {
                rtdbRef.child("telecallers").setValue(rtdbAgents).await()
            } else {
                rtdbRef.child("telecallers").child("_welcome_placeholder_").setValue(
                    mapOf(
                        "info" to "Collection initialized successfully. Awaiting caller profile allocation matrix tables.",
                        "status" to "EMPTY",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }

            // 5. RTDB Security Settings
            _syncEvents.emit("RTDB Category 5/7: Syncing security compliance policies...")
            rtdbRef.child("security_settings_audit").child("current_policies").setValue(securitySettings).await()

            // 6. RTDB Sync Settings
            _syncEvents.emit("RTDB Category 6/7: Syncing system configurations...")
            rtdbRef.child("sync_settings_audit").child("current_criteria").setValue(syncSettings).await()

            // 7. RTDB Telecaller Remarks Wise Call History Collection (Grouping)
            _syncEvents.emit("RTDB Category 7/7: Syncing telecaller and remarks-wise grouped archives...")
            if (groupedLogs.isNotEmpty()) {
                for ((key, logsForGroup) in groupedLogs) {
                    val currentAgentId = key.first
                    val currentOutcome = key.second
                    val rawSlug = currentOutcome.replace("/", "_").replace(" ", "_").trim()

                    val matchingAgent = agents.firstOrNull { it.id == currentAgentId }
                    val currentAgentName = matchingAgent?.name ?: logsForGroup.firstOrNull()?.agentName ?: "Rajesh Kumar"

                    val groupDetails = mutableMapOf<String, Any>()
                    for (log in logsForGroup) {
                        val debtorName = debtors.firstOrNull { it.id == log.debtorId }?.name ?: "Unknown"
                        val cleanId = sanitizeKey(log.id)
                        groupDetails[cleanId] = mapOf(
                            "id" to log.id,
                            "debtorId" to log.debtorId,
                            "debtorName" to debtorName,
                            "timestamp" to log.callTimestamp,
                            "date" to log.date,
                            "time" to log.time,
                            "outcome" to log.callDisposition,
                            "remarks" to (log.agentNotes ?: log.notes),
                            "callType" to log.callType,
                            "agentId" to currentAgentId,
                            "agentName" to currentAgentName
                        )
                    }
                    rtdbRef.child("telecaller_remarks_wise").child(currentAgentId).child(rawSlug).setValue(groupDetails).await()
                }
            }

                _syncEvents.emit("RTDB Success: All mirroring databases uploaded and confirmed.")
            }
        } catch (rtdbEx: Exception) {
            _syncEvents.emit("RTDB Warning: Mirror check skipped/failed -> ${rtdbEx.localizedMessage}")
            android.util.Log.e("FirebaseSync", "Failed to sync to Realtime Database: ${rtdbEx.message}", rtdbEx)
        }

        if (overallSuccess) {
            _syncEvents.emit("SUCCESS: Category-wise Cloud Sync finished perfectly!")
        } else {
            _syncEvents.emit("WARNING: Category-wise Cloud Sync completed with issues.")
        }
        overallSuccess
    }
}
