package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<String?>

    suspend fun login(username: String, password: String): Result<Unit>
    suspend fun register(username: String, password: String): Result<Unit>
    suspend fun registerTelecallerWithoutLogin(
        username: String,
        password: String,
        email: String = "",
        phoneNumber: String = "",
        isAdmin: Boolean = false
    ): Result<Unit>
    suspend fun logout()
}
