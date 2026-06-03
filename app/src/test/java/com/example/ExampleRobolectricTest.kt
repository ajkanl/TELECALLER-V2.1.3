package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CollectionDatabase
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.PromiseToPayEntity
import com.example.data.repository.DebtorRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dues Recovery", appName)
  }

  @Test
  fun `database and repository setup validation`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    
    val debtorDao = db.debtorDao()
    val callLogDao = db.callLogDao()
    
    assertNotNull(debtorDao)
    assertNotNull(callLogDao)
    
    val repo = DebtorRepositoryImpl(debtorDao, callLogDao)
    val debtors = repo.getDebtors().first()
    assertNotNull(debtors)
    db.close()
  }

  @Test
  fun `collection database mapping and dao test suite`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, CollectionDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val dao = db.collectionDao()
    assertNotNull(dao)

    // 1. Insert a list of DebtorEntity records
    val debtor = DebtorEntity(
        id = "L_ACC_101",
        name = "Karan Johar",
        phoneNumber = "+919999888877",
        alternativeNumber = "+918888777766",
        totalOverdueAmount = 75000.0,
        principalAmount = 70000.0,
        dpdBucket = "60-90",
        allocationDate = System.currentTimeMillis() - 432000000L,
        currentStatus = "PENDING"
    )
    dao.insertOrUpdateDebtors(listOf(debtor))

    // 2. Fetch debtor by phone number
    val debtorByPhone = dao.getDebtorByNumber("+919999888877").first()
    assertNotNull(debtorByPhone)
    assertEquals("Karan Johar", debtorByPhone?.name)

    // 3. Fetch debtor by alternative phone number
    val debtorByAltPhone = dao.getDebtorByNumber("+918888777766").first()
    assertNotNull(debtorByAltPhone)
    assertEquals("L_ACC_101", debtorByAltPhone?.id)

    // 4. Fetch debtor by DPD bucket sorted by totalOverdueAmount descending
    val dpdList = dao.getDebtorsByDpdBucket("60-90").first()
    assertEquals(1, dpdList.size)
    assertEquals(75000.0, dpdList.first().totalOverdueAmount, 0.001)

    // 5. Insert call log action
    val callLogEntry = CallLogEntity(
        debtorId = "L_ACC_101",
        callTimestamp = System.currentTimeMillis() - 3600000L,
        durationSeconds = 88,
        callType = "OUTBOUND",
        callDisposition = "ANSWERED",
        agentNotes = "Debtor promised settlement tomorrow",
        recordingFilePath = "/sdcard/recordings/call_101.wav"
    )
    dao.insertCallLog(callLogEntry)

    // 6. Insert Promise-to-Pay (PTP) Record
    val ptpDate = System.currentTimeMillis() + 86400000L // 1 day future
    val ptpRecord = PromiseToPayEntity(
        debtorId = "L_ACC_101",
        ptpCreationTimestamp = System.currentTimeMillis(),
        promisedPaymentDate = ptpDate,
        promisedAmount = 45000.0,
        ptpStatus = "ACTIVE"
    )
    dao.insertOrUpdatePromiseToPay(ptpRecord)

    // 7. Verify JOIN fetch with Room @Relation transaction using range query
    val startRange = System.currentTimeMillis()
    val endRange = System.currentTimeMillis() + 2 * 86400000L
    val promisesWithDebtor = dao.getPromisesToPayWithDebtorInRange(startRange, endRange).first()
    
    assertEquals(1, promisesWithDebtor.size)
    val resultMatched = promisesWithDebtor.first()
    assertEquals(45000.0, resultMatched.promiseToPay.promisedAmount, 0.001)
    assertEquals("L_ACC_101", resultMatched.debtor.id)
    assertEquals("Karan Johar", resultMatched.debtor.name)

    db.close()
  }
}

