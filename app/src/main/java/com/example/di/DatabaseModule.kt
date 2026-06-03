package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.CollectionDatabase
import com.example.data.local.dao.CallLogDao
import com.example.data.local.dao.CollectionDao
import com.example.data.local.dao.DebtorDao
import com.example.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dues_recovery_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideCollectionDatabase(@ApplicationContext context: Context): CollectionDatabase {
        return Room.databaseBuilder(
            context,
            CollectionDatabase::class.java,
            "collection_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideDebtorDao(db: AppDatabase): DebtorDao = db.debtorDao()

    @Provides
    fun provideCallLogDao(db: AppDatabase): CallLogDao = db.callLogDao()

    @Provides
    @Singleton
    fun provideCollectionDao(db: CollectionDatabase): CollectionDao = db.collectionDao()
}
