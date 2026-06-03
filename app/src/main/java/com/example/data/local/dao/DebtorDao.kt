package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DebtorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtorDao {
    @Query("SELECT * FROM debtors")
    fun getAllDebtors(): Flow<List<DebtorEntity>>

    @Query("SELECT * FROM debtors WHERE id = :id LIMIT 1")
    fun getDebtorById(id: String): Flow<DebtorEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtors(debtors: List<DebtorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtor(debtor: DebtorEntity)

    @Update
    suspend fun updateDebtor(debtor: DebtorEntity)
}
