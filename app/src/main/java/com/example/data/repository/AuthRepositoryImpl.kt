package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.domain.repository.AuthRepository
import com.example.domain.security.SecuritySettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
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
        
        val isEmail = trimmedUser.contains("@")
        val isPhone = trimmedUser.replace(Regex("[^\\d+]"), "").length >= 10
        val finalEmail = if (isEmail) trimmedUser.lowercase() else ""
        val finalPhone = if (isPhone) trimmedUser else ""
        val finalUsername = if (isEmail) trimmedUser.substringBefore("@") else trimmedUser
        val isAdminUser = trimmedUser.contains("admin") || finalEmail == "armankumar.singh24@gmail.com"

        val newUser = UserEntity(
            username = finalUsername,
            passwordHash = hashPassword(password),
            email = finalEmail,
            phoneNumber = finalPhone,
            isAdmin = isAdminUser
        )
        userDao.insertUser(newUser)

        // Write to Google Firebase Database (Firestore) under authorized_users collection
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userMap = mapOf(
                "username" to finalUsername,
                "email" to finalEmail,
                "phoneNumber" to finalPhone,
                "isAdmin" to isAdminUser,
                "createdTime" to System.currentTimeMillis()
            )
            val docId = if (finalEmail.isNotBlank()) finalEmail else if (finalPhone.isNotBlank()) finalPhone else finalUsername
            db.collection("authorized_users").document(docId).set(userMap)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to sync registered user to Firestore on signup: ${e.message}", e)
        }

        // Write to Google Firebase Realtime Database (RTDB) under authorized_users node
        try {
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://telecalller-pro-default-rtdb.firebaseio.com")
            val userMap = mapOf(
                "username" to finalUsername,
                "email" to finalEmail,
                "phoneNumber" to finalPhone,
                "isAdmin" to isAdminUser,
                "createdTime" to System.currentTimeMillis()
            )
            val pathKey = if (finalEmail.isNotBlank()) {
                finalEmail.replace(".", "_")
            } else if (finalPhone.isNotBlank()) {
                finalPhone.replace("+", "")
            } else {
                finalUsername
            }
            rtdb.getReference("authorized_users").child(pathKey).setValue(userMap).await()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to sync registered user to RTDB on signup: ${e.message}", e)
        }

        _currentUser.value = finalUsername
        _isLoggedIn.value = true
        securitySettingsStore.addTelecaller(finalUsername, isAdminUser)
        securitySettingsStore.handleAgentLogin(finalUsername)
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

        // Write to Google Firebase Database (Firestore) under authorized_users collection
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userMap = mapOf(
                "username" to trimmedUser,
                "email" to email.trim().lowercase(),
                "phoneNumber" to phoneNumber.trim(),
                "isAdmin" to isAdmin,
                "createdTime" to System.currentTimeMillis()
            )
            val docId = if (email.isNotBlank()) email.trim().lowercase() else if (phoneNumber.isNotBlank()) phoneNumber.trim() else trimmedUser
            db.collection("authorized_users").document(docId).set(userMap)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to sync authorized user to Firestore: ${e.message}", e)
        }

        // Write to Google Firebase Realtime Database (RTDB) under authorized_users node
        try {
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://telecalller-pro-default-rtdb.firebaseio.com")
            val userMap = mapOf(
                "username" to trimmedUser,
                "email" to email.trim().lowercase(),
                "phoneNumber" to phoneNumber.trim(),
                "isAdmin" to isAdmin,
                "createdTime" to System.currentTimeMillis()
            )
            val pathKey = if (email.isNotBlank()) {
                email.trim().lowercase().replace(".", "_")
            } else if (phoneNumber.isNotBlank()) {
                phoneNumber.trim().replace("+", "")
            } else {
                trimmedUser
            }
            rtdb.getReference("authorized_users").child(pathKey).setValue(userMap).await()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to sync authorized user to RTDB: ${e.message}", e)
        }

        return Result.success(Unit)
    }

    override suspend fun loginWithGoogle(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(Exception("Google email cannot be empty"))
        }
        val cleanEmail = email.trim().lowercase()
        
        // 1. Try checking in Google Firebase Database (Realtime Database - RTDB)
        try {
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://telecalller-pro-default-rtdb.firebaseio.com")
            val snap = rtdb.getReference("authorized_users")
                .orderByChild("email")
                .equalTo(cleanEmail)
                .get()
                .await()

            if (snap.exists() && snap.hasChildren()) {
                val matchedNode = snap.children.first()
                val username = matchedNode.child("username").getValue(String::class.java) ?: cleanEmail.substringBefore("@")
                val isAdmin = matchedNode.child("isAdmin").getValue(Boolean::class.java) ?: false
                
                var localUser = userDao.getUserByUsername(username)
                if (localUser == null) {
                    localUser = UserEntity(
                        username = username,
                        passwordHash = hashPassword("firebase_authenticated_google"),
                        email = cleanEmail,
                        isAdmin = isAdmin
                    )
                    userDao.insertUser(localUser)
                    securitySettingsStore.addTelecaller(username, isAdmin)
                }
                
                _currentUser.value = username
                _isLoggedIn.value = true
                securitySettingsStore.handleAgentLogin(username)
                return Result.success(Unit)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Firebase RTDB auth lookup failed: ${e.message}", e)
        }

        // 2. Try checking in Google Firebase Database (Firestore)
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val querySnapshot = db.collection("authorized_users")
                .whereEqualTo("email", cleanEmail)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val doc = querySnapshot.documents.first()
                val username = doc.getString("username") ?: cleanEmail.substringBefore("@")
                val isAdmin = doc.getBoolean("isAdmin") ?: false
                
                var localUser = userDao.getUserByUsername(username)
                if (localUser == null) {
                    localUser = UserEntity(
                        username = username,
                        passwordHash = hashPassword("firebase_authenticated_google"),
                        email = cleanEmail,
                        isAdmin = isAdmin
                    )
                    userDao.insertUser(localUser)
                    securitySettingsStore.addTelecaller(username, isAdmin)
                }
                
                _currentUser.value = username
                _isLoggedIn.value = true
                securitySettingsStore.handleAgentLogin(username)
                return Result.success(Unit)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Firebase Firestore auth lookup failed: ${e.message}. Using fallback.", e)
        }

        // 2. Local fallback
        val existingUserWithEmail = userDao.getUserByEmail(cleanEmail)
        if (existingUserWithEmail != null) {
            val name = existingUserWithEmail.username
            _currentUser.value = name
            _isLoggedIn.value = true
            securitySettingsStore.handleAgentLogin(name)
            return Result.success(Unit)
        }

        // Demo fallback for testing
        if (cleanEmail == "armankumar.singh24@gmail.com" || cleanEmail.startsWith("admin") || cleanEmail.startsWith("telecaller")) {
            val isDemoAdmin = cleanEmail.contains("admin") || cleanEmail == "armankumar.singh24@gmail.com"
            val demoUser = cleanEmail.substringBefore("@")
            val newUser = UserEntity(
                username = demoUser,
                passwordHash = hashPassword("demo_password"),
                email = cleanEmail,
                isAdmin = isDemoAdmin
            )
            userDao.insertUser(newUser)
            securitySettingsStore.addTelecaller(demoUser, isDemoAdmin)
            _currentUser.value = demoUser
            _isLoggedIn.value = true
            securitySettingsStore.handleAgentLogin(demoUser)
            return Result.success(Unit)
        }

        return Result.failure(Exception("This Google Account is not registered/authorized in the Firebase database."))
    }

    override suspend fun loginWithPhone(phoneNumber: String): Result<Unit> {
        val cleanPhone = phoneNumber.replace(Regex("[^\\d+]"), "").trim()
        if (cleanPhone.isBlank()) {
            return Result.failure(Exception("Phone number cannot be empty"))
        }

        // 1. Try checking in Google Firebase Database (Realtime Database - RTDB)
        try {
            val rtdb = com.google.firebase.database.FirebaseDatabase.getInstance("https://telecalller-pro-default-rtdb.firebaseio.com")
            val snap = rtdb.getReference("authorized_users")
                .orderByChild("phoneNumber")
                .equalTo(cleanPhone)
                .get()
                .await()

            if (snap.exists() && snap.hasChildren()) {
                val matchedNode = snap.children.first()
                val username = matchedNode.child("username").getValue(String::class.java) ?: "PhoneUser_${cleanPhone.takeLast(4)}"
                val isAdmin = matchedNode.child("isAdmin").getValue(Boolean::class.java) ?: false
                
                var localUser = userDao.getUserByUsername(username)
                if (localUser == null) {
                    localUser = UserEntity(
                        username = username,
                        passwordHash = hashPassword("firebase_authenticated_phone"),
                        phoneNumber = cleanPhone,
                        isAdmin = isAdmin
                    )
                    userDao.insertUser(localUser)
                    securitySettingsStore.addTelecaller(username, isAdmin)
                }

                _currentUser.value = username
                _isLoggedIn.value = true
                securitySettingsStore.handleAgentLogin(username)
                return Result.success(Unit)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Firebase RTDB phone lookup failed: ${e.message}", e)
        }

        // 2. Try checking in Google Firebase Database (Firestore)
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val querySnapshot = db.collection("authorized_users")
                .whereEqualTo("phoneNumber", cleanPhone)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val doc = querySnapshot.documents.first()
                val username = doc.getString("username") ?: "PhoneUser_${cleanPhone.takeLast(4)}"
                val isAdmin = doc.getBoolean("isAdmin") ?: false
                
                var localUser = userDao.getUserByUsername(username)
                if (localUser == null) {
                    localUser = UserEntity(
                        username = username,
                        passwordHash = hashPassword("firebase_authenticated_phone"),
                        phoneNumber = cleanPhone,
                        isAdmin = isAdmin
                    )
                    userDao.insertUser(localUser)
                    securitySettingsStore.addTelecaller(username, isAdmin)
                }

                _currentUser.value = username
                _isLoggedIn.value = true
                securitySettingsStore.handleAgentLogin(username)
                return Result.success(Unit)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Firebase Firestore phone auth remote lookup failed: ${e.message}", e)
        }

        // 2. Local fallback find user by Phone
        val matchedLocalUser = userDao.getUserByPhone(cleanPhone)
        if (matchedLocalUser != null) {
            val name = matchedLocalUser.username
            _currentUser.value = name
            _isLoggedIn.value = true
            securitySettingsStore.handleAgentLogin(name)
            return Result.success(Unit)
        }

        // Demo fallback for common debug numbers
        if (cleanPhone.endsWith("9876543210") || cleanPhone == "1234567890" || cleanPhone.endsWith("9999999999") || cleanPhone == "8516098672") {
            val demoUser = "Demo_Agent_${cleanPhone.takeLast(4)}"
            val newUser = UserEntity(
                username = demoUser,
                passwordHash = hashPassword("demo_phone_password"),
                phoneNumber = cleanPhone,
                isAdmin = cleanPhone.endsWith("9876543210")
            )
            userDao.insertUser(newUser)
            securitySettingsStore.addTelecaller(demoUser, cleanPhone.endsWith("9876543210"))
            _currentUser.value = demoUser
            _isLoggedIn.value = true
            securitySettingsStore.handleAgentLogin(demoUser)
            return Result.success(Unit)
        }

        return Result.failure(Exception("This phone number is not listed/authorized in the Firebase database."))
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
        return password.hashCode().toString()
    }
}
