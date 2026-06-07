package com.example.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CollectionDatabase
import com.example.domain.repository.AuthRepository
import com.example.domain.security.SecuritySettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appDatabase: AppDatabase,
    private val collectionDatabase: CollectionDatabase,
    private val securitySettingsStore: SecuritySettingsStore
) : ViewModel() {

    val isNumberMaskingEnabled: StateFlow<Boolean> = securitySettingsStore.isNumberMaskingEnabled
    val isHardwareBindingEnabled: StateFlow<Boolean> = securitySettingsStore.isHardwareBindingEnabled
    val isScreenshotBlockEnabled: StateFlow<Boolean> = securitySettingsStore.isScreenshotBlockEnabled

    // Delegation of new features
    val themeMode: StateFlow<String> = securitySettingsStore.themeMode
    val activeImpersonatedAgent: StateFlow<com.example.domain.security.TelecallerAgent?> = securitySettingsStore.activeImpersonatedAgent
    val telecallersList: StateFlow<List<com.example.domain.security.TelecallerAgent>> = securitySettingsStore.telecallersList

    fun toggleNumberMasking(enabled: Boolean) {
        securitySettingsStore.toggleNumberMasking(enabled)
    }

    fun toggleHardwareBinding(enabled: Boolean) {
        securitySettingsStore.toggleHardwareBinding(enabled)
    }

    fun toggleScreenshotBlock(enabled: Boolean) {
        securitySettingsStore.toggleScreenshotBlock(enabled)
    }

    fun setThemeMode(mode: String) {
        securitySettingsStore.setThemeMode(mode)
    }

    fun setImpersonatedAgent(agent: com.example.domain.security.TelecallerAgent?) {
        securitySettingsStore.setImpersonatedAgent(agent)
    }

    fun addTelecaller(name: String, isAdmin: Boolean) {
        securitySettingsStore.addTelecaller(name, isAdmin)
    }

    fun removeTelecaller(name: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = authRepository.deleteTelecaller(name)
            onResult(result)
        }
    }

    fun registerTelecaller(
        name: String,
        passwordPlain: String,
        email: String,
        phoneNumber: String,
        isAdmin: Boolean,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.registerTelecallerWithoutLogin(
                username = name,
                password = passwordPlain,
                email = email,
                phoneNumber = phoneNumber,
                isAdmin = isAdmin
            )
            onResult(result)
        }
    }

    fun updateAgentPermissions(agentId: String, permissions: com.example.domain.security.AgentPermissions, isDisabled: Boolean = false) {
        securitySettingsStore.updateAgentPermissions(agentId, permissions, isDisabled)
    }

    fun updateAgentTarget(agentId: String, targetAmount: Double) {
        securitySettingsStore.updateAgentTarget(agentId, targetAmount)
    }

    fun updateAgentMonthlyTarget(agentId: String, month: String, amount: Double) {
        securitySettingsStore.updateAgentMonthlyTarget(agentId, month, amount)
    }

    fun updateAgentProfile(agentId: String, name: String, profilePicture: String?) {
        securitySettingsStore.updateAgentProfile(agentId, name, profilePicture)
    }

    /**
     * Instantly clear all local SQLite databases, remove remote Firestore/RTDB records, and terminate session via AuthRepository logout.
     */
    fun executeEmergencyPurge(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 1. Delete Firestore collections completely
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val collections = listOf(
                        "debtors",
                        "call_logs",
                        "promises_to_pay",
                        "telecallers",
                        "security_settings_audit",
                        "sync_settings_audit",
                        "telecaller_remarks_wise",
                        "authorized_users"
                    )
                    for (colName in collections) {
                        try {
                            if (colName == "telecaller_remarks_wise") {
                                val snapshot = db.collection(colName).get().await()
                                for (doc in snapshot.documents) {
                                    val subSnapshot = db.collection(colName).document(doc.id).collection("calls").get().await()
                                    for (subDoc in subSnapshot.documents) {
                                        db.collection(colName).document(doc.id).collection("calls").document(subDoc.id).delete().await()
                                    }
                                    db.collection(colName).document(doc.id).delete().await()
                                }
                            } else {
                                val snapshot = db.collection(colName).get().await()
                                for (doc in snapshot.documents) {
                                    db.collection(colName).document(doc.id).delete().await()
                                }
                            }
                            android.util.Log.d("SecurityViewModel", "Cleared Firestore collection: $colName")
                        } catch (ex: Exception) {
                            android.util.Log.e("SecurityViewModel", "Failed to clear Firestore collection $colName: ${ex.message}", ex)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SecurityViewModel", "Could not purge Firestore data", e)
                }

                // 2. Delete Google Firebase Realtime Database node structures
                try {
                    val rtdb = com.example.data.util.FirebaseDatabaseConnector.getInstance()
                    if (rtdb != null) {
                        val children = listOf(
                            "debtors",
                            "call_logs",
                            "promises_to_pay",
                            "telecallers",
                            "security_settings_audit",
                            "sync_settings_audit",
                            "telecaller_remarks_wise",
                            "authorized_users"
                        )
                        for (child in children) {
                            try {
                                rtdb.getReference(child).removeValue().await()
                                android.util.Log.d("SecurityViewModel", "Cleared RTDB child: $child")
                            } catch (ex: Exception) {
                                android.util.Log.e("SecurityViewModel", "Failed to clear RTDB child $child: ${ex.message}", ex)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SecurityViewModel", "Could not purge RTDB data", e)
                }

                // 3. Clear local databases
                try {
                    // Instantly clear all tables as required
                    appDatabase.clearAllTables()
                    collectionDatabase.clearAllTables()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            securitySettingsStore.resetToZero()
            authRepository.logout()
            onComplete()
        }
    }

    fun maskPhoneNumber(number: String): String {
        return securitySettingsStore.maskPhoneNumber(number)
    }
}
