package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.PromiseToPayEntity
import com.example.data.local.entity.PromiseToPayWithDebtor
import com.example.data.local.entity.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) providing database operations for the Dues Recovery models:
 * DebtorEntity, CallLogEntity, and PromiseToPayEntity.
 */
@Dao
interface CollectionDao {

    /**
     * 1. Insert or update a list of 'DebtorEntity' records in bulk (On Conflict Strategy: Replace).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDebtors(debtors: List<DebtorEntity>)

    /**
     * 2. Fetch a single 'DebtorEntity' by its 'phoneNumber' or 'alternativeNumber' (Used for call state lookups).
     * Returns a reactive Flow that notifies of database changes.
     */
    @Query("SELECT * FROM debtors WHERE phoneNumber = :number OR alternativeNumber = :number LIMIT 1")
    fun getDebtorByNumber(number: String): Flow<DebtorEntity?>

    /**
     * Alternately, get debtor by number synchronously or with suspend context.
     */
    @Query("SELECT * FROM debtors WHERE phoneNumber = :number OR alternativeNumber = :number LIMIT 1")
    suspend fun getDebtorByNumberImmediate(number: String): DebtorEntity?

    /**
     * 3. Fetch all debtors belonging to a specific 'dpdBucket' (Days Past Due), sorted descending by 'totalOverdueAmount'.
     */
    @Query("SELECT * FROM debtors WHERE dpdBucket = :dpdBucket ORDER BY totalOverdueAmount DESC")
    fun getDebtorsByDpdBucket(dpdBucket: String): Flow<List<DebtorEntity>>

    /**
     * 4. Insert a 'CallLogEntity' record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity)

    /**
     * 5. Insert or update a 'PromiseToPayEntity'.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePromiseToPay(promiseToPay: PromiseToPayEntity): Long

    /**
     * 6. Custom @Query to fetch all 'PromiseToPayEntity' records where 'promisedPaymentDate' matches
     * a given timestamp range (to show "PTPs Due Today") alongside their corresponding 'DebtorEntity'
     * details using a Room '@Relation' transaction object.
     */
    @Transaction
    @Query("SELECT * FROM promises_to_pay WHERE promisedPaymentDate BETWEEN :startTimestamp AND :endTimestamp")
    fun getPromisesToPayWithDebtorInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<PromiseToPayWithDebtor>>

    /**
     * Immediate (non-Flow) retrieval for the range-based query.
     */
    @Transaction
    @Query("SELECT * FROM promises_to_pay WHERE promisedPaymentDate BETWEEN :startTimestamp AND :endTimestamp")
    suspend fun getPromisesToPayWithDebtorInRangeImmediate(startTimestamp: Long, endTimestamp: Long): List<PromiseToPayWithDebtor>

    @Query("SELECT * FROM promises_to_pay")
    fun getAllPromisesToPay(): Flow<List<PromiseToPayEntity>>

    @Query("SELECT * FROM debtors")
    fun getAllDebtorsList(): Flow<List<DebtorEntity>>

    @Query("SELECT * FROM call_logs")
    fun getAllCallLogsList(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM debtors")
    suspend fun getDebtorsDirect(): List<DebtorEntity>

    @Query("SELECT * FROM promises_to_pay")
    suspend fun getPromisesToPayDirect(): List<PromiseToPayEntity>

    @Query("SELECT * FROM call_logs")
    suspend fun getCallLogsDirect(): List<CallLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentHistoryEntity)

    @Query("SELECT * FROM payment_history WHERE debtorId = :debtorId ORDER BY paymentDate DESC")
    fun getPaymentsForDebtor(debtorId: String): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT * FROM payment_history")
    fun getAllPaymentsFlow(): Flow<List<PaymentHistoryEntity>>

    /**
     * Fetch debtors eligible for the active calling queue based on strict business timing constraints.
     */
    @Query("""
        SELECT * FROM debtors
        WHERE currentStatus != 'SETTLED'
          AND NOT EXISTS (
              SELECT 1 FROM call_logs
              WHERE call_logs.debtorId = debtors.id
                AND call_logs.callTimestamp >= :fifteenDaysAgoTimestamp
          )
          AND NOT EXISTS (
              SELECT 1 FROM promises_to_pay
              WHERE promises_to_pay.debtorId = debtors.id
                AND promises_to_pay.ptpStatus = 'ACTIVE'
                AND (
                    promises_to_pay.promisedPaymentDate > :currentTimestamp
                    OR (
                        promises_to_pay.promisedPaymentDate <= :currentTimestamp
                        AND promises_to_pay.promisedPaymentDate >= :fifteenDaysAgoTimestamp
                        AND promises_to_pay.promisedPaymentDate BETWEEN :startOfMonthTimestamp AND :endOfMonthTimestamp
                    )
                )
          )
    """)
    fun getEligibleCallingQueue(
        currentTimestamp: Long,
        fifteenDaysAgoTimestamp: Long,
        startOfMonthTimestamp: Long,
        endOfMonthTimestamp: Long
    ): Flow<List<DebtorEntity>>

    /**
     * Fetch debtors eligible for the high-priority Follow-up Queue based on strict target dates and calendar month rules.
     */
    @Query("""
        SELECT * FROM debtors
        WHERE currentStatus != 'SETTLED'
          AND NOT EXISTS (
              SELECT 1 FROM call_logs
              WHERE call_logs.debtorId = debtors.id
                AND call_logs.callTimestamp >= :fifteenDaysAgoTimestamp
          )
          AND EXISTS (
              SELECT 1 FROM promises_to_pay
              WHERE promises_to_pay.debtorId = debtors.id
                AND promises_to_pay.ptpStatus = 'ACTIVE'
                AND promises_to_pay.promisedPaymentDate <= :currentTimestamp
                AND promises_to_pay.promisedPaymentDate >= :fifteenDaysAgoTimestamp
                AND promises_to_pay.promisedPaymentDate BETWEEN :startOfMonthTimestamp AND :endOfMonthTimestamp
          )
    """)
    fun getEligibleFollowUpQueue(
        currentTimestamp: Long,
        fifteenDaysAgoTimestamp: Long,
        startOfMonthTimestamp: Long,
        endOfMonthTimestamp: Long
    ): Flow<List<DebtorEntity>>
}

