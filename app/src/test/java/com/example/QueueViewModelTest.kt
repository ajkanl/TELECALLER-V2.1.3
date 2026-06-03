package com.example

import com.example.data.config.AppRemoteConfigManager
import com.example.data.local.dao.CollectionDao
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.PromiseToPayEntity
import com.example.data.local.entity.PromiseToPayWithDebtor
import com.example.presentation.queue.QueueViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class QueueViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeCollectionDao : CollectionDao {
        var capturedCurrentTimestamp: Long = 0
        var capturedFifteenDaysAgoTimestamp: Long = 0
        var capturedStartOfMonthTimestamp: Long = 0
        var capturedEndOfMonthTimestamp: Long = 0

        override suspend fun insertOrUpdateDebtors(debtors: List<DebtorEntity>) {}
        override fun getDebtorByNumber(number: String): Flow<DebtorEntity?> = flowOf(null)
        override suspend fun getDebtorByNumberImmediate(number: String): DebtorEntity? = null
        override fun getDebtorsByDpdBucket(dpdBucket: String): Flow<List<DebtorEntity>> = flowOf(emptyList())
        override suspend fun insertCallLog(callLog: CallLogEntity) {}
        override suspend fun insertOrUpdatePromiseToPay(promiseToPay: PromiseToPayEntity) {}
        override fun getPromisesToPayWithDebtorInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<PromiseToPayWithDebtor>> = flowOf(emptyList())
        override suspend fun getPromisesToPayWithDebtorInRangeImmediate(startTimestamp: Long, endTimestamp: Long): List<PromiseToPayWithDebtor> = emptyList()
        override fun getAllPromisesToPay(): Flow<List<PromiseToPayEntity>> = flowOf(emptyList())
        override fun getAllDebtorsList(): Flow<List<DebtorEntity>> = flowOf(emptyList())
        override fun getAllCallLogsList(): Flow<List<CallLogEntity>> = flowOf(emptyList())
        override suspend fun getDebtorsDirect(): List<DebtorEntity> = emptyList()
        override suspend fun getPromisesToPayDirect(): List<PromiseToPayEntity> = emptyList()
        override suspend fun getCallLogsDirect(): List<CallLogEntity> = emptyList()

        override fun getEligibleCallingQueue(
            currentTimestamp: Long,
            fifteenDaysAgoTimestamp: Long,
            startOfMonthTimestamp: Long,
            endOfMonthTimestamp: Long
        ): Flow<List<DebtorEntity>> {
            capturedCurrentTimestamp = currentTimestamp
            capturedFifteenDaysAgoTimestamp = fifteenDaysAgoTimestamp
            capturedStartOfMonthTimestamp = startOfMonthTimestamp
            capturedEndOfMonthTimestamp = endOfMonthTimestamp
            return flowOf(listOf(
                DebtorEntity(
                    id = "TEST-ACCT",
                    name = "Test Student",
                    phoneNumber = "+919876543210",
                    alternativeNumber = null,
                    totalOverdueAmount = 1000.0,
                    principalAmount = 900.0,
                    dpdBucket = "1-30",
                    allocationDate = currentTimestamp,
                    currentStatus = "PENDING"
                )
            ))
        }

        override fun getEligibleFollowUpQueue(
            currentTimestamp: Long,
            fifteenDaysAgoTimestamp: Long,
            startOfMonthTimestamp: Long,
            endOfMonthTimestamp: Long
        ): Flow<List<DebtorEntity>> {
            return flowOf(emptyList())
        }
    }

    @Test
    fun queueViewModel_correctlyCalculatesTimeBoundariesAndQueriesDao() = runTest(testDispatcher) {
        val fakeDao = FakeCollectionDao()
        val fakeConfigManager = AppRemoteConfigManager(null)
        val viewModel = QueueViewModel(fakeDao, fakeConfigManager)

        // Force execution and collect flow emission when populated asynchronously
        val reminderList = viewModel.reminderQueueList.filter { it.isNotEmpty() }.first()

        assertEquals(1, reminderList.size)
        assertEquals("TEST-ACCT", reminderList[0].id)

        // Verify that parameters closely matches current system state calculations
        val systemNow = Instant.now().toEpochMilli()
        assertTrue(fakeDao.capturedCurrentTimestamp <= systemNow)
        assertTrue(fakeDao.capturedCurrentTimestamp > systemNow - 5000)

        // 15 days ago Check
        val approx15DaysAgo = systemNow - (15L * 24 * 60 * 60 * 1000)
        assertTrue(fakeDao.capturedFifteenDaysAgoTimestamp <= approx15DaysAgo + 2000)
        assertTrue(fakeDao.capturedFifteenDaysAgoTimestamp >= approx15DaysAgo - 2000)

        // Start month Check
        val expectedStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expectedStart, fakeDao.capturedStartOfMonthTimestamp)

        // End month Check
        val expectedEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expectedEnd, fakeDao.capturedEndOfMonthTimestamp)
    }
}
