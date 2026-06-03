package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DebtorEntity
import com.example.data.repository.FirebaseSyncRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirebaseSyncRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var syncRepository: FirebaseSyncRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        syncRepository = FirebaseSyncRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun syncBulkDataFromCloud_insertsRecordsUnderSingleTransactionAndEmitsSuccess() = runBlocking {
        val debtorsToSync = listOf(
            DebtorEntity(
                id = "ACT101",
                name = "Aman Singh",
                phoneNumber = "+919988776655",
                alternativeNumber = null,
                totalOverdueAmount = 50000.0,
                principalAmount = 45000.0,
                dpdBucket = "1-30",
                allocationDate = System.currentTimeMillis(),
                currentStatus = "PENDING"
            ),
            DebtorEntity(
                id = "ACT102",
                name = "Harshita Sharma",
                phoneNumber = "+919876543210",
                alternativeNumber = "0987654321",
                totalOverdueAmount = 25000.0,
                principalAmount = 22000.0,
                dpdBucket = "31-60",
                allocationDate = System.currentTimeMillis(),
                currentStatus = "PENDING"
            )
        )

        // Capture emitted event in real time Flow collection
        val events = mutableListOf<String>()
        val job = launch {
            syncRepository.syncEvents.collect {
                events.add(it)
            }
        }

        // Run bulk sync
        syncRepository.syncBulkDataFromCloud(debtorsToSync)

        // Read inserted items from database to verify transaction-level persistence
        val insertedDebtors = database.debtorDao().getAllDebtors().first()
        assertEquals(2, insertedDebtors.size)
        
        val firstDebtor = insertedDebtors.find { it.id == "ACT101" }
        val secondDebtor = insertedDebtors.find { it.id == "ACT102" }
        
        assertEquals("Aman Singh", firstDebtor?.name)
        assertEquals("Harshita Sharma", secondDebtor?.name)

        // Verify emitted completion notifications
        assertEquals(1, events.size)
        assertEquals("Sync Complete: 2 Records Updated Localized", events[0])

        job.cancel()
    }
}
