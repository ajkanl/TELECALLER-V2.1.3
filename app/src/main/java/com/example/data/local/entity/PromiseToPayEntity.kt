package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing Promise-to-Pay (PTP) collection commitments made by a debtor.
 */
@Entity(
    tableName = "promises_to_pay",
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
data class PromiseToPayEntity(
    @PrimaryKey(autoGenerate = true) val ptpId: Long = 0,
    val debtorId: String,
    val ptpCreationTimestamp: Long,
    val promisedPaymentDate: Long,
    val promisedAmount: Double,
    val ptpStatus: String // e.g., "ACTIVE", "KEPT", "BROKEN"
)
