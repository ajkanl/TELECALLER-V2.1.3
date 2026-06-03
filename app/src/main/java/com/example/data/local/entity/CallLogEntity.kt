package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing communication session logs that persist collection calls.
 * Implements a ForeignKey link to the parent DebtorEntity with a CASCADE delete constraint.
 */
@Entity(
    tableName = "call_logs",
    foreignKeys = [
        ForeignKey(
            entity = DebtorEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["debtorId"])
    ]
)
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val callId: Long = 0,
    val debtorId: String,
    val callTimestamp: Long,
    val durationSeconds: Long,
    val callType: String, // e.g. "OUTBOUND", "INBOUND"
    val callDisposition: String, // e.g. "ANSWERED", "RINGING_NO_ANSWER", "WRONG_NUMBER", "REFUSED_TO_PAY"
    val agentNotes: String?,
    val recordingFilePath: String?,

    // Backward-compatibility properties with safe fallback values
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: String = "",
    val time: String = "",
    val outcome: String = callDisposition,
    val notes: String = agentNotes ?: ""
)
