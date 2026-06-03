package com.example.presentation.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.config.AppRemoteConfigManager
import com.example.data.local.dao.CollectionDao
import com.example.data.local.entity.DebtorEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

/**
 * Data representation of calculated millisecond epoch boundaries.
 */
data class TimeBoundaries(
    val currentTimeMillis: Long,
    val fifteenDaysAgoMillis: Long, // kept for backward compatibility with query / parameter name
    val startOfMonthMillis: Long,
    val endOfMonthMillis: Long
)

/**
 * ViewModel responsible for managing and distributing filtered recovery queues
 * with precise time calculations driven by Calendar and Remote Config.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QueueViewModel @Inject constructor(
    private val collectionDao: CollectionDao,
    private val appRemoteConfigManager: AppRemoteConfigManager
) : ViewModel() {

    // Internal calculation routine constantly re-evaluating boundaries in real time (e.g. every 10 seconds)
    private val timeBoundariesFlow: Flow<TimeBoundaries> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(10000L) // Refresh timing parameters on a recurring thread-safe interval
        }
    }.combine(appRemoteConfigManager.cooldownDaysStandardFlow) { timestamp, cooldownDays ->
        val cooldownDaysAgoMillis = timestamp - (cooldownDays.toLong() * 24L * 60L * 60L * 1000L)

        // Start of month
        val startCalendar = Calendar.getInstance()
        startCalendar.timeInMillis = timestamp
        startCalendar.set(Calendar.DAY_OF_MONTH, 1)
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        val startOfMonthMillis = startCalendar.timeInMillis

        // End of month
        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = timestamp
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        endCalendar.set(Calendar.MILLISECOND, 999)
        val endOfMonthMillis = endCalendar.timeInMillis

        TimeBoundaries(
            currentTimeMillis = timestamp,
            fifteenDaysAgoMillis = cooldownDaysAgoMillis,
            startOfMonthMillis = startOfMonthMillis,
            endOfMonthMillis = endOfMonthMillis
        )
    }

    // Standard Dues Reminder collection queue reactive state flow
    val reminderQueueList: StateFlow<List<DebtorEntity>> = timeBoundariesFlow
        .flatMapLatest { boundaries ->
            collectionDao.getEligibleCallingQueue(
                currentTimestamp = boundaries.currentTimeMillis,
                fifteenDaysAgoTimestamp = boundaries.fifteenDaysAgoMillis,
                startOfMonthTimestamp = boundaries.startOfMonthMillis,
                endOfMonthTimestamp = boundaries.endOfMonthMillis
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // High priority Follow-up collection queue reactive state flow
    val followUpQueueList: StateFlow<List<DebtorEntity>> = timeBoundariesFlow
        .flatMapLatest { boundaries ->
            collectionDao.getEligibleFollowUpQueue(
                currentTimestamp = boundaries.currentTimeMillis,
                fifteenDaysAgoTimestamp = boundaries.fifteenDaysAgoMillis,
                startOfMonthTimestamp = boundaries.startOfMonthMillis,
                endOfMonthTimestamp = boundaries.endOfMonthMillis
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
