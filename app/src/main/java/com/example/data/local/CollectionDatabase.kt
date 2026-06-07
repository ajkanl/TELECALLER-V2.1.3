package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.CollectionDao
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.PromiseToPayEntity
import com.example.data.local.entity.PaymentHistoryEntity

/**
 * Modern Jetpack Room Database configuration for Recovery Applet collections.
 * Persists debt accounts, call summaries, and collector promise commitments securely.
 */
@Database(
    entities = [
        DebtorEntity::class,
        CallLogEntity::class,
        PromiseToPayEntity::class,
        PaymentHistoryEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class CollectionDatabase : RoomDatabase() {

    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: CollectionDatabase? = null

        /**
         * Thread-safe Singleton implementation to guarantee a single instance
         * of CollectionDatabase is instantiated and shared across the application memory space.
         */
        fun getInstance(context: Context): CollectionDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CollectionDatabase::class.java,
                    "collection_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
