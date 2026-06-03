package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a debtor recovery account in local SQLite persistence.
 * Indices are added on the primary caller number for efficient lookup during incoming call broadcasts.
 */
@Entity(
    tableName = "debtors",
    indices = [
        Index(value = ["phoneNumber"])
    ]
)
data class DebtorEntity(
    @PrimaryKey val id: String, // Loan Account Number
    val name: String,
    val phoneNumber: String, // Indexed for fast telephony hook matching
    val alternativeNumber: String?,
    val totalOverdueAmount: Double,
    val principalAmount: Double,
    val dpdBucket: String, // Days Past Due classification, e.g., "1-30", "31-60", "60-90", "90+"
    val allocationDate: Long, // Temporal allocation epoch timestamp
    val currentStatus: String, // e.g., "PENDING", "PTP", "BROKEN_PTP", "SETTLED"

    // Backward-compatibility properties with safe defaults to ensure non-breaking integration
    val contactNumber: String = phoneNumber,
    val address: String = "",
    val outstandingAmount: Double = totalOverdueAmount,
    val lastContactDate: String = "0 Days Ago",
    val customerSegment: String = "Standard",
    val college: String = "Default College",
    val remarks: String = "",
    val father: String = "",
    val dob: String = "",
    val course: String = "",
    val courseSession: String = "",
    val guardianNumber: String = ""
)
