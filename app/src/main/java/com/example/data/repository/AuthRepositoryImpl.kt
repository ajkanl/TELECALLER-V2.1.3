package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.domain.repository.AuthRepository
import com.example.domain.security.SecuritySettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val securitySettingsStore: SecuritySettingsStore
) : AuthRepository {

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<String?>(null)
    override val currentUser: Flow<String?> = _currentUser.asStateFlow()

    override suspend fun login(username: String, password: String): Result<Unit> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Username and password cannot be empty"))
        }
        val trimmedUser = username.trim()
        
        // Auto-create admin user if it does not exist yet
        if (trimmedUser.equals("admin", ignoreCase = true)) {
            val existingAdmin = userDao.getUserByUsername("admin")
            if (existingAdmin == null) {
                userDao.insertUser(
                    UserEntity(
                        username = "admin",
                        passwordHash = hashPassword("admin123")
                    )
                )
            }
        }

        val user = userDao.getUserByUsername(trimmedUser)
        return if (user != null && user.passwordHash == hashPassword(password)) {
            _currentUser.value = user.username
            _isLoggedIn.value = true
            securitySettingsStore.handleAgentLogin(user.username)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }

    override suspend fun register(username: String, password: String): Result<Unit> {
        val trimmedUser = username.trim()
        if (trimmedUser.length < 3) {
            return Result.failure(Exception("Username must be at least 3 characters"))
        }
        if (password.length < 4) {
            return Result.failure(Exception("Password must be at least 4 characters"))
        }
        val existing = userDao.getUserByUsername(trimmedUser)
        if (existing != null) {
            return Result.failure(Exception("Username is already taken"))
        }
        
        val newUser = UserEntity(
            username = trimmedUser,
            passwordHash = hashPassword(password)
        )
        userDao.insertUser(newUser)
        _currentUser.value = trimmedUser
        _isLoggedIn.value = true
        securitySettingsStore.handleAgentLogin(trimmedUser)
        return Result.success(Unit)
    }

    override suspend fun registerTelecallerWithoutLogin(
        username: String,
        password: String,
        email: String,
        phoneNumber: String,
        isAdmin: Boolean
    ): Result<Unit> {
        val trimmedUser = username.trim()
        if (trimmedUser.length < 3) {
            return Result.failure(Exception("Username must be at least 3 characters"))
        }
        if (password.length < 4) {
            return Result.failure(Exception("Password must be at least 4 characters"))
        }
        val existing = userDao.getUserByUsername(trimmedUser)
        if (existing != null) {
            return Result.failure(Exception("Username is already taken"))
        }
        val newUser = UserEntity(
            username = trimmedUser,
            passwordHash = hashPassword(password),
            email = email,
            phoneNumber = phoneNumber,
            isAdmin = isAdmin
        )
        userDao.insertUser(newUser)
        // Create matching telecaller agent automatically in offline list
        securitySettingsStore.addTelecaller(trimmedUser, isAdmin)
        return Result.success(Unit)
    }

    override suspend fun logout() {
        val current = _currentUser.value
        if (current != null) {
            securitySettingsStore.handleAgentLogout(current)
        }
        _isLoggedIn.value = false
        _currentUser.value = null
    }

    private fun hashPassword(password: String): String {
        // Simple hash function for persistence demonstration
        return password.hashCode().toString()
    }
}
