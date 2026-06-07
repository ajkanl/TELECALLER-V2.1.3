package com.example.domain.repository

import com.example.domain.model.CallRecord
import com.example.domain.model.Debtor
import kotlinx.coroutines.flow.Flow

interface DebtorRepository {
    fun getDebtors(): Flow<List<Debtor>>
    fun getRecentCalls(): Flow<List<CallRecord>>
    suspend fun recordCall(debtorName: String, status: String, simCard: String)
    suspend fun recordCallWithNotes(debtorId: String, debtorName: String, status: String, simCard: String, notes: String)
    fun getDailyCallProgress(): Flow<Pair<Int, Int>> // returns Pair(completed, target)
    fun getDailyRecoveredAmount(): Flow<Double>
    
    // Detailed Profile Editing & Room database persistence
    suspend fun updateDebtor(debtor: Debtor)
    fun getCallLogsForDebtor(debtorId: String): Flow<List<CallRecord>>
    suspend fun saveCallLog(debtorId: String, debtorName: String, date: String, time: String, outcome: String, notes: String, agentId: String = "T01", agentName: String = "Rajesh Kumar", ptpDate: String? = null, ptpAmount: Double? = null)
}
