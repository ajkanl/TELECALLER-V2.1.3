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
    private val callLogDao: CallLogDao,
    private val collectionDao: com.example.data.local.dao.CollectionDao
) : DebtorRepository {

    private val dailyProgress = MutableStateFlow(Pair(0, 50))
    private val recoveredAmount = MutableStateFlow(0.0)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existing = debtorDao.getAllDebtors().first()
                if (existing.isEmpty()) {
                    val csvText = """
HIMANSHU BHARDWAJ;AKHILESH SINGH;2003-06-23;;GNM;2024-2027;5;8448522928;8448522928
Sachin Mallick;Jityendra Mallick;2001-02-05;;GNM;2024-2027;0;8908099089;8908099089
test new;tesyy;2020-06-09;;GNM;2024-2027;0;8787878787;8787878787
test;Uday Singh;2002-01-01;;GNM;2024-2027;2;8908990890;8089789789
Archna Singh;DP SINGH;2001-01-28;;GNM;2024-2027;123123;;8779878978
Sarthak Kumar;Uday Singh;2002-01-05;;GNM;2024-2027;232;8908990890;7897897887
vaibhav khatri;Satish;2005-12-16;;GNM;2024-2027;83;8798598678;8295382085
                    """.trimIndent()
                    
                    val lines = csvText.split("\n")
                    val entities = mutableListOf<DebtorEntity>()
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.isEmpty()) continue
                        val tokens = trimmed.split(";")
                        if (tokens.size < 9) continue
                        val name = tokens[0].trim()
                        val father = tokens[1].trim()
                        val dob = tokens[2].trim()
                        val college = tokens[3].trim().ifBlank { "Default College" }
                        val course = tokens[4].trim()
                        val courseSession = tokens[5].trim()
                        val rollStr = tokens[6].trim()
                        val parentNum = tokens[7].trim()
                        val phone = tokens[8].trim()
                        
                        val id = if (rollStr != "0" && rollStr.isNotEmpty()) rollStr else "R-${phone}"
                        
                        val entity = DebtorEntity(
                            id = id,
                            name = name,
                            phoneNumber = "+91" + phone,
                            alternativeNumber = if (parentNum.isNotEmpty()) "+91" + parentNum else null,
                            totalOverdueAmount = 15000.0,
                            principalAmount = 15000.0,
                            dpdBucket = "1-30",
                            allocationDate = System.currentTimeMillis(),
                            currentStatus = "PENDING",
                            contactNumber = "+91" + phone,
                            address = "Default Address",
                            outstandingAmount = 15000.0,
                            lastContactDate = dob.ifBlank { "Never" },
                            customerSegment = "Standard",
                            college = college,
                            remarks = if (father.isNotEmpty()) "Father: $father" else "",
                            father = father,
                            dob = dob,
                            course = course,
                            courseSession = courseSession,
                            guardianNumber = parentNum
                        )
                        entities.add(entity)
                    }
                    
                    if (entities.isNotEmpty()) {
                        debtorDao.insertDebtors(entities)
                        collectionDao.insertOrUpdateDebtors(entities)
                    }
                }
            } catch (e: Exception) {
                Log.e("DebtorRepositoryImpl", "Verification or initialization seeding failed", e)
            }
        }
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
                    remarks = entity.remarks,
                    father = entity.father,
                    dob = entity.dob,
                    course = entity.course,
                    courseSession = entity.courseSession,
                    guardianNumber = entity.guardianNumber
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
            collectionDao.insertOrUpdateDebtors(listOf(updated))
        }
    }

    override fun getDailyCallProgress(): Flow<Pair<Int, Int>> = dailyProgress

    override fun getDailyRecoveredAmount(): Flow<Double> = recoveredAmount

    override suspend fun updateDebtor(debtor: Debtor) {
        val resolvedId = if (debtor.id.isBlank()) "STU-${System.currentTimeMillis()}" else debtor.id
        val entity = DebtorEntity(
            id = resolvedId,
            name = debtor.name,
            phoneNumber = debtor.phoneNumber,
            alternativeNumber = if (debtor.guardianNumber.isNotBlank()) debtor.guardianNumber else null,
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
            college = debtor.college.ifBlank { "Unmapped" },
            remarks = debtor.remarks,
            father = debtor.father,
            dob = debtor.dob,
            course = debtor.course,
            courseSession = debtor.courseSession,
            guardianNumber = debtor.guardianNumber
        )
        val list = debtorDao.getAllDebtors().first()
        val exists = list.any { it.id == resolvedId }
        if (exists) {
            debtorDao.updateDebtor(entity)
        } else {
            debtorDao.insertDebtor(entity)
        }
        collectionDao.insertOrUpdateDebtors(listOf(entity))
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
            collectionDao.insertOrUpdateDebtors(listOf(updated))
        }
    }
}
