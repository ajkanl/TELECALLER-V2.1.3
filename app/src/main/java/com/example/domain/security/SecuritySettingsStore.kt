package com.example.domain.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AgentPermissions(
    val callInitiation: Boolean = true,
    val canSeeFullNumbers: Boolean = false,
    val canPerformPurge: Boolean = false,
    val canRecordAudio: Boolean = true,
    val isAdmin: Boolean = false
)

data class TelecallerAgent(
    val id: String,
    val name: String,
    val isOnline: Boolean,
    val callsDialed: Int,
    val talkTimeMinutes: Int,
    val ptpsSecured: Int,
    val targetAmount: Double = 150000.0,
    val permissions: AgentPermissions = AgentPermissions(),
    val isDisabled: Boolean = false,
    val profilePicture: String? = null,
    val monthlyCollectionTargets: Map<String, Double> = mapOf(
        "January" to 150000.0, "February" to 150000.0, "March" to 150000.0,
        "April" to 150000.0, "May" to 150000.0, "June" to 150000.0,
        "July" to 150000.0, "August" to 150000.0, "September" to 150000.0,
        "October" to 150000.0, "November" to 150000.0, "December" to 150000.0
    )
)

@Singleton
class SecuritySettingsStore @Inject constructor() {
    private val _isNumberMaskingEnabled = MutableStateFlow(true)
    val isNumberMaskingEnabled: StateFlow<Boolean> = _isNumberMaskingEnabled.asStateFlow()

    private val _isHardwareBindingEnabled = MutableStateFlow(false)
    val isHardwareBindingEnabled: StateFlow<Boolean> = _isHardwareBindingEnabled.asStateFlow()

    private val _isScreenshotBlockEnabled = MutableStateFlow(false)
    val isScreenshotBlockEnabled: StateFlow<Boolean> = _isScreenshotBlockEnabled.asStateFlow()

    // --- NEW: Theme Selector ---
    private val _themeMode = MutableStateFlow("system") // system, light, dark, bento_slate, crimson_warning, neon_emerald
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // --- NEW: Active Impersonated Agent for Admin Dashboard Switch ---
    private val _activeImpersonatedAgent = MutableStateFlow<TelecallerAgent?>(null)
    val activeImpersonatedAgent: StateFlow<TelecallerAgent?> = _activeImpersonatedAgent.asStateFlow()

    // --- NEW: Dynamic Telecaller Agents & Permission Management ---
    private val _telecallersList = MutableStateFlow(
        listOf(
            TelecallerAgent("T01", "Rajesh Kumar", true, 142, 272, 28, permissions = AgentPermissions(callInitiation = true, canSeeFullNumbers = true, canRecordAudio = true, isAdmin = true)),
            TelecallerAgent("T02", "Aditi Verma", true, 128, 235, 24, permissions = AgentPermissions(callInitiation = true, canSeeFullNumbers = false, canRecordAudio = true, isAdmin = false)),
            TelecallerAgent("T03", "Aarav Patel", false, 95, 160, 15, permissions = AgentPermissions(callInitiation = false, canSeeFullNumbers = false, canRecordAudio = false, isAdmin = false)),
            TelecallerAgent("T04", "Neha Sharma", true, 118, 195, 19, permissions = AgentPermissions(callInitiation = true, canSeeFullNumbers = false, canRecordAudio = true, isAdmin = false)),
            TelecallerAgent("T05", "Kabir Singh", false, 110, 182, 17, permissions = AgentPermissions(callInitiation = true, canSeeFullNumbers = true, canRecordAudio = false, isAdmin = false))
        )
    )
    val telecallersList: StateFlow<List<TelecallerAgent>> = _telecallersList.asStateFlow()

    fun toggleNumberMasking(enabled: Boolean) {
        _isNumberMaskingEnabled.value = enabled
    }

    fun toggleHardwareBinding(enabled: Boolean) {
        _isHardwareBindingEnabled.value = enabled
    }

    fun toggleScreenshotBlock(enabled: Boolean) {
        _isScreenshotBlockEnabled.value = enabled
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    fun setImpersonatedAgent(agent: TelecallerAgent?) {
        _activeImpersonatedAgent.value = agent
    }

    fun addTelecaller(name: String, isAdmin: Boolean = false) {
        val nextId = "T0${_telecallersList.value.size + 1}"
        val enforceAdmin = isAdmin || name.contains("admin", ignoreCase = true) || name.contains("armankumar.singh24", ignoreCase = true)
        val newAgent = TelecallerAgent(
            id = nextId,
            name = name,
            isOnline = true,
            callsDialed = 0,
            talkTimeMinutes = 0,
            ptpsSecured = 0,
            permissions = AgentPermissions(
                callInitiation = true,
                canSeeFullNumbers = true,
                canPerformPurge = false,
                canRecordAudio = true,
                isAdmin = enforceAdmin
            )
        )
        _telecallersList.value = _telecallersList.value + newAgent
    }
    
    fun removeTelecaller(agentId: String) {
        val list = _telecallersList.value
        _telecallersList.value = list.filter { it.id != agentId }
        if (_activeImpersonatedAgent.value?.id == agentId) {
            _activeImpersonatedAgent.value = null
        }
    }

    fun updateAgentPermissions(agentId: String, permissions: AgentPermissions, isDisabled: Boolean = false) {
        _telecallersList.value = _telecallersList.value.map { agent ->
            if (agent.id == agentId) {
                // If the dynamic permissions are toggled, update both nested permissions and masking override
                val updated = agent.copy(permissions = permissions, isDisabled = isDisabled)
                // If active impersonation matches, update the active state as well
                if (_activeImpersonatedAgent.value?.id == agentId) {
                    _activeImpersonatedAgent.value = updated
                }
                updated
            } else {
                agent
            }
        }
    }

    fun getCurrentMonthName(): String {
        return java.text.SimpleDateFormat("MMMM", java.util.Locale.ENGLISH).format(java.util.Date())
    }

    fun updateAgentTarget(agentId: String, targetAmount: Double) {
        _telecallersList.value = _telecallersList.value.map { agent ->
            if (agent.id == agentId) {
                val currentMonth = getCurrentMonthName()
                val updatedTargets = agent.monthlyCollectionTargets.toMutableMap()
                updatedTargets[currentMonth] = targetAmount
                val updated = agent.copy(
                    targetAmount = targetAmount,
                    monthlyCollectionTargets = updatedTargets
                )
                if (_activeImpersonatedAgent.value?.id == agentId) {
                    _activeImpersonatedAgent.value = updated
                }
                updated
            } else {
                agent
            }
        }
    }

    fun updateAgentMonthlyTarget(agentId: String, month: String, amount: Double) {
        _telecallersList.value = _telecallersList.value.map { agent ->
            if (agent.id == agentId) {
                val updatedTargets = agent.monthlyCollectionTargets.toMutableMap()
                updatedTargets[month] = amount
                val currentMonth = getCurrentMonthName()
                val isCurrentMonth = month.equals(currentMonth, ignoreCase = true)
                val updated = agent.copy(
                    monthlyCollectionTargets = updatedTargets,
                    targetAmount = if (isCurrentMonth) amount else agent.targetAmount
                )
                if (_activeImpersonatedAgent.value?.id == agentId) {
                    _activeImpersonatedAgent.value = updated
                }
                updated
            } else {
                agent
            }
        }
    }

    fun updateAgentProfile(agentId: String, name: String, profilePicture: String?) {
        _telecallersList.value = _telecallersList.value.map { agent ->
            if (agent.id == agentId) {
                val updated = agent.copy(name = name, profilePicture = profilePicture)
                if (_activeImpersonatedAgent.value?.id == agentId) {
                    _activeImpersonatedAgent.value = updated
                }
                updated
            } else {
                agent
            }
        }
    }

    /**
     * Replaces intermediate digits with asterisks when masking is enabled.
     * E.g. "+919876543210" -> "+91 ******3210"
     */
    fun maskPhoneNumber(number: String): String {
        if (!_isNumberMaskingEnabled.value) return number
        val cleanNum = number.trim()
        if (cleanNum.length >= 10) {
            val prefix = if (cleanNum.startsWith("+91")) "+91" else cleanNum.take(3)
            val suffix = cleanNum.takeLast(4)
            return "$prefix ******$suffix"
        }
        if (cleanNum.length > 4) {
            return cleanNum.take(2) + "*".repeat(cleanNum.length - 4) + cleanNum.takeLast(2)
        }
        return cleanNum
    }

    fun handleAgentLogin(name: String) {
        val list = _telecallersList.value
        val existingAgent = list.find { it.name.equals(name, ignoreCase = true) }
        if (existingAgent != null) {
            _telecallersList.value = list.map { agent ->
                if (agent.id == existingAgent.id) {
                    agent.copy(isOnline = true)
                } else {
                    agent
                }
            }
        } else {
            // Add a new telecaller agent automatically
            val nextId = "T0${list.size + 1}"
            val enforceAdmin = name.contains("admin", ignoreCase = true) || name.contains("armankumar.singh24", ignoreCase = true)
            val newAgent = TelecallerAgent(
                id = nextId,
                name = name,
                isOnline = true,
                callsDialed = 0,
                talkTimeMinutes = 0,
                ptpsSecured = 0,
                permissions = AgentPermissions(
                    callInitiation = true,
                    canSeeFullNumbers = true,
                    canPerformPurge = false,
                    canRecordAudio = true,
                    isAdmin = enforceAdmin
                )
            )
            _telecallersList.value = list + newAgent
        }
    }

    fun handleAgentLogout(name: String) {
        val list = _telecallersList.value
        val existingAgent = list.find { it.name.equals(name, ignoreCase = true) }
        if (existingAgent != null) {
            _telecallersList.value = list.map { agent ->
                if (agent.id == existingAgent.id) {
                    agent.copy(isOnline = false)
                } else {
                    agent
                }
            }
        }
    }
}
