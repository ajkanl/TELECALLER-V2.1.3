package com.example.presentation.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.config.AppRemoteConfigManager
import com.example.data.local.dao.CollectionDao
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.PromiseToPayEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SmartCallingQueueViewModel @Inject constructor(
    private val collectionDao: CollectionDao,
    private val appRemoteConfigManager: AppRemoteConfigManager
) : ViewModel() {

    // Helper functions for month boundaries
    private fun getStartOfMonthTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonthTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private val currentTimestamp: Long
        get() = System.currentTimeMillis()

    // Config-driven cooldown duration in milliseconds flow
    private val cooldownDurationMillisFlow: Flow<Long> = appRemoteConfigManager.cooldownDaysStandardFlow
        .map { it * 24L * 60 * 60 * 1000 }

    // Flow for standard Calling Queue (Dues reminder queue)
    val eligibleCallingQueue: StateFlow<List<DebtorEntity>> = cooldownDurationMillisFlow.flatMapLatest { cooldownMillis ->
        collectionDao.getEligibleCallingQueue(
            currentTimestamp,
            currentTimestamp - cooldownMillis,
            getStartOfMonthTimestamp(),
            getEndOfMonthTimestamp()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow for Active Follow-up Queue
    val eligibleFollowUpQueue: StateFlow<List<DebtorEntity>> = cooldownDurationMillisFlow.flatMapLatest { cooldownMillis ->
        collectionDao.getEligibleFollowUpQueue(
            currentTimestamp,
            currentTimestamp - cooldownMillis,
            getStartOfMonthTimestamp(),
            getEndOfMonthTimestamp()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Debtors from CollectionDatabase
    val allDebtors: StateFlow<List<DebtorEntity>> = collectionDao.getAllDebtorsList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Call Logs from CollectionDatabase
    val allCallLogs: StateFlow<List<CallLogEntity>> = collectionDao.getAllCallLogsList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Promises from CollectionDatabase
    val allPromises: StateFlow<List<PromiseToPayEntity>> = collectionDao.getAllPromisesToPay()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Handled empty - no seed data
    }

    fun seedInitialQueueDataIfNeeded() {
        // Disabled demo seeding to preserve completely clean environment
    }
}
