package com.example.di

import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.DebtorRepositoryImpl
import com.example.data.repository.FirebaseSyncRepositoryImpl
import com.example.data.repository.GeminiRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.DebtorRepository
import com.example.domain.repository.FirebaseSyncRepository
import com.example.domain.repository.GeminiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDebtorRepository(
        debtorRepositoryImpl: DebtorRepositoryImpl
    ): DebtorRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFirebaseSyncRepository(
        firebaseSyncRepositoryImpl: FirebaseSyncRepositoryImpl
    ): FirebaseSyncRepository

    @Binds
    @Singleton
    abstract fun bindGeminiRepository(
        geminiRepositoryImpl: GeminiRepositoryImpl
    ): GeminiRepository
}

