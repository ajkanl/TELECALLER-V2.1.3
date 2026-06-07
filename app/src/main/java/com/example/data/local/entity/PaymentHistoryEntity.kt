package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing payment history records registered for a debtor student account.
 * Supports Cash, Online (with UTR Number), and Cheque (with Cheque Number and Date of Cashing).
 */
@Entity(
    tableName = "payment_history",
    foreignKeys = [
        ForeignKey(
            entity = DebtorEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["debtorId"])]
)
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtorId: String,
    val paymentType: String, // e.g., "CASH", "ONLINE", "CHEQUE"
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val utrNumber: String? = null,
    val chequeNumber: String? = null,
    val chequeCashingDate: Long? = null, // timestamp when cheque should be cashed
    val remarks: String = ""
)
