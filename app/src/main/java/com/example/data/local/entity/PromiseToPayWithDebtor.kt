package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A transaction data class uniting PromiseToPayEntity with its corresponding DebtorEntity
 * using Room's @Relation annotation.
 */
data class PromiseToPayWithDebtor(
    @Embedded val promiseToPay: PromiseToPayEntity,
    
    @Relation(
        parentColumn = "debtorId",
        entityColumn = "id"
    )
    val debtor: DebtorEntity
)
