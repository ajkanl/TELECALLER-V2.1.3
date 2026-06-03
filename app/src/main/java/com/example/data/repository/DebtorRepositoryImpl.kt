package com.example.data.repository

import android.util.Log
import com.example.data.local.dao.CallLogDao
import com.example.data.local.dao.DebtorDao
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.domain.model.CallRecord
import com.example.domain.model.Debtor
import com.example.domain.repository.DebtorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtorRepositoryImpl @Inject constructor(
    private val debtorDao: DebtorDao,
    private val callLogDao: CallLogDao
) : DebtorRepository {

    private val dailyProgress = MutableStateFlow(Pair(0, 50))
    private val recoveredAmount = MutableStateFlow(0.0)

    init {
        // Safe empty: No pre-population of demo data for deployment to maintain user's clean importing state
    }

    override fun getDebtors(): Flow<List<Debtor>> {
        return debtorDao.getAllDebtors().map { list ->
            list.map { entity ->
                Debtor(
                    id = entity.id,
                    name = entity.name,
                    overdueDays = try { entity.lastContactDate.removeSuffix(" Days Ago").trim().toInt() } catch(e: Exception) { 10 },
                    outstandingAmount = entity.outstandingAmount,
                    customerSegment = entity.customerSegment,
                    phoneNumber = entity.contactNumber,
                    address = entity.address,
                    lastContactDate = entity.lastContactDate,
                    college = entity.college,
                    remarks = entity.remarks
                )
            }
        }
    }

    override fun getRecentCalls(): Flow<List<CallRecord>> {
        return debtorDao.getAllDebtors().combine(callLogDao.getAllCallLogs()) { debtors, logs ->
            logs.map { log ->
                val name = debtors.firstOrNull { it.id == log.debtorId }?.name ?: "Unknown"
                CallRecord(
                    id = log.id,
                    debtorId = log.debtorId,
                    debtorName = name,
                    status = log.outcome,
                    time = log.time,
                    simCard = "SIM 1",
                    date = log.date,
                    notes = log.notes
                )
            }
        }
    }

    override suspend fun recordCall(debtorName: String, status: String, simCard: String) {
        val debtors = debtorDao.getAllDebtors().first()
        val match = debtors.firstOrNull { it.name.equals(debtorName, ignoreCase = true) }
        val id = match?.id ?: "1"
        recordCallWithNotes(
            debtorId = id,
            debtorName = debtorName,
            status = status,
            simCard = simCard,
            notes = "Recorded quick call."
        )
    }

    override suspend fun recordCallWithNotes(
        debtorId: String,
        debtorName: String,
        status: String,
        simCard: String,
        notes: String
    ) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val newCall = CallLogEntity(
            debtorId = debtorId,
            callTimestamp = System.currentTimeMillis(),
            durationSeconds = 15,
            callType = "OUTBOUND",
            callDisposition = status,
            agentNotes = notes,
            recordingFilePath = null,
            id = UUID.randomUUID().toString(),
            date = currentDate,
            time = currentTime,
            outcome = status,
            notes = notes
        )
        callLogDao.insertCallLog(newCall)

        // Increment daily call progress count
        val currentPair = dailyProgress.value
        dailyProgress.value = Pair(currentPair.first + 1, currentPair.second)

        // Mock payment recovery update if status is PTP Promised
        if (status == "PTP Promised") {
            recoveredAmount.value += 15000.00
        }

        // Also update lastContactDate of the debtor
        val debtorList = debtorDao.getAllDebtors().first()
        val currentDebtor = debtorList.firstOrNull { it.id == debtorId }
        if (currentDebtor != null) {
            val updated = currentDebtor.copy(lastContactDate = "0 Days Ago")
            debtorDao.updateDebtor(updated)
        }
    }

    override fun getDailyCallProgress(): Flow<Pair<Int, Int>> = dailyProgress

    override fun getDailyRecoveredAmount(): Flow<Double> = recoveredAmount

    override suspend fun updateDebtor(debtor: Debtor) {
        val entity = DebtorEntity(
            id = debtor.id,
            name = debtor.name,
            phoneNumber = debtor.phoneNumber,
            alternativeNumber = null,
            totalOverdueAmount = debtor.outstandingAmount,
            principalAmount = debtor.outstandingAmount * 0.9,
            dpdBucket = "1-30",
            allocationDate = System.currentTimeMillis(),
            currentStatus = "PENDING",
            contactNumber = debtor.phoneNumber,
            address = debtor.address,
            outstandingAmount = debtor.outstandingAmount,
            lastContactDate = debtor.lastContactDate,
            customerSegment = debtor.customerSegment,
            college = debtor.college,
            remarks = debtor.remarks
        )
        debtorDao.updateDebtor(entity)
    }

    override fun getCallLogsForDebtor(debtorId: String): Flow<List<CallRecord>> {
        return callLogDao.getCallLogsForDebtor(debtorId).map { list ->
            list.map { entity ->
                CallRecord(
                    id = entity.id,
                    debtorId = entity.debtorId,
                    debtorName = "",
                    status = entity.outcome,
                    time = entity.time,
                    simCard = "SIM 1",
                    date = entity.date,
                    notes = entity.notes
                )
            }
        }
    }

    override suspend fun saveCallLog(
        debtorId: String,
        debtorName: String,
        date: String,
        time: String,
        outcome: String,
        notes: String
    ) {
        val newCallLog = CallLogEntity(
            debtorId = debtorId,
            callTimestamp = System.currentTimeMillis(),
            durationSeconds = 30,
            callType = "OUTBOUND",
            callDisposition = outcome,
            agentNotes = notes,
            recordingFilePath = null,
            id = UUID.randomUUID().toString(),
            date = date,
            time = time,
            outcome = outcome,
            notes = notes
        )
        callLogDao.insertCallLog(newCallLog)

        // Increment daily call progress count
        val currentPair = dailyProgress.value
        dailyProgress.value = Pair(currentPair.first + 1, currentPair.second)

        // Mock payment recovery update if status is PTP Promised
        if (outcome == "PTP Promised") {
            recoveredAmount.value += 15000.00
        }

        // Update last contacted status
        val debtorList = debtorDao.getAllDebtors().first()
        val currentDebtor = debtorList.firstOrNull { it.id == debtorId }
        if (currentDebtor != null) {
            val updated = currentDebtor.copy(lastContactDate = "0 Days Ago")
            debtorDao.updateDebtor(updated)
        }
    }
}
