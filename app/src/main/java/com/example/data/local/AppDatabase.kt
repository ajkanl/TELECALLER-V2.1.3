package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.CallLogDao
import com.example.data.local.dao.CollectionDao
import com.example.data.local.dao.DebtorDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.PromiseToPayEntity

@Database(
    entities = [UserEntity::class, DebtorEntity::class, CallLogEntity::class, PromiseToPayEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun debtorDao(): DebtorDao
    abstract fun callLogDao(): CallLogDao
    abstract fun collectionDao(): CollectionDao
}
