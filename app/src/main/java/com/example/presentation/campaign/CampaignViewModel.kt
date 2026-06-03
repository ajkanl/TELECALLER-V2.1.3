package com.example.presentation.campaign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.CollectionDao
import com.example.data.local.entity.DebtorEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Summary metric showing recovery performance of different delinquent buckets
data class CampaignMetric(
    val name: String,
    val description: String,
    val recoveryRate: Int,
    val totalAccounts: Int,
    val colorHex: Long
)

@HiltViewModel
class CampaignViewModel @Inject constructor(
    private val collectionDao: CollectionDao
) : ViewModel() {

    // 1. Horizontal Scroll Metric summaries
    private val _campaignMetrics = MutableStateFlow(
        listOf(
            CampaignMetric("Bucket 1", "1-30 DPD Priority", 45, 124, 0xFF3B82F6),
            CampaignMetric("Bucket 2", "31-60 DPD Mid-Tier", 22, 85, 0xFF10B981),
            CampaignMetric("Bucket 3", "61-90 DPD Delinquent", 12, 60, 0xFFF59E0B),
            CampaignMetric("Bucket 4", "90+ DPD Critical", 5, 42, 0xFFEF4444)
        )
    )
    val campaignMetrics: StateFlow<List<CampaignMetric>> = _campaignMetrics.asStateFlow()

    // Pools and Agents
    val dpdPools = listOf(
        "1-30 Days Past Due",
        "31-60 Days Past Due",
        "61-90 Days Past Due",
        "90+ Days Past Due"
    )

    val agentTeams = listOf(
        "Tier 1: Core Telecallers",
        "Tier 2: Experienced Negotiators",
        "Tier 3: Intensive Recovery Squad",
        "Tier 4: Legal & Field Recovery Unit"
    )

    // 2. State-driven selection variables
    private val _selectedSourcePool = MutableStateFlow("61-90 Days Past Due")
    val selectedSourcePool: StateFlow<String> = _selectedSourcePool.asStateFlow()

    private val _selectedTargetAgent = MutableStateFlow("Tier 2: Experienced Negotiators")
    val selectedTargetAgent: StateFlow<String> = _selectedTargetAgent.asStateFlow()

    // 3. Queue freeze switch state
    private val _isDialerQueueFrozen = MutableStateFlow(false)
    val isDialerQueueFrozen: StateFlow<Boolean> = _isDialerQueueFrozen.asStateFlow()

    // Execution workflow state parameters
    private val _isReallocating = MutableStateFlow(false)
    val isReallocating: StateFlow<Boolean> = _isReallocating.asStateFlow()

    private val _reallocationResultMsg = MutableStateFlow<String?>(null)
    val reallocationResultMsg: StateFlow<String?> = _reallocationResultMsg.asStateFlow()

    fun setSelectedSourcePool(pool: String) {
        _selectedSourcePool.value = pool
    }

    fun setSelectedTargetAgent(agent: String) {
        _selectedTargetAgent.value = agent
    }

    fun toggleDialerQueueFrozen(frozen: Boolean) {
        _isDialerQueueFrozen.value = frozen
    }

    fun clearResultMsg() {
        _reallocationResultMsg.value = null
    }

    /**
     * Executes the re-allocation workflow: queries matchable DPD entries and updates status
     */
    fun triggerReAllocationWorkflow() {
        viewModelScope.launch {
            _isReallocating.value = true
            _reallocationResultMsg.value = null

            val sourcePool = _selectedSourcePool.value
            val targetTeam = _selectedTargetAgent.value

            // Corresponds to matching bucket prefix
            val bucketPrefix = when (sourcePool) {
                "1-30 Days Past Due" -> "1-30"
                "31-60 Days Past Due" -> "31-60"
                "61-90 Days Past Due" -> "61-90"
                else -> "90+"
            }

            withContext(Dispatchers.IO) {
                try {
                    // Fetch matching debtors by bucket
                    val debtors = collectionDao.getDebtorsByDpdBucket(bucketPrefix).firstOrNull() ?: emptyList()
                    
                    if (debtors.isNotEmpty()) {
                        val updatedDebtors = debtors.map { debtor ->
                            // Update allocation tag or segment parameter as simulated Firestore backend re-allocation
                            val suffix = targetTeam.substringBefore(":")
                            debtor.copy(
                                customerSegment = "Re-routed to $suffix",
                                currentStatus = "PENDING"
                            )
                        }
                        collectionDao.insertOrUpdateDebtors(updatedDebtors)
                        
                        // Update metrics representation slightly to reflect the adjustment
                        _campaignMetrics.value = _campaignMetrics.value.map { metric ->
                            if (metric.description.contains(bucketPrefix)) {
                                metric.copy(totalAccounts = metric.totalAccounts + updatedDebtors.size)
                            } else {
                                metric
                            }
                        }

                        _reallocationResultMsg.value = "Success: Re-routed ${updatedDebtors.size} delinquent accounts safely to $targetTeam!"
                    } else {
                        _reallocationResultMsg.value = "Notice: No active debtor accounts found in the local pipeline matching DPD Bucket $bucketPrefix to migrate."
                    }
                } catch (e: Exception) {
                    _reallocationResultMsg.value = "Failed: Error compiling migration parameters: ${e.message}"
                } finally {
                    _isReallocating.value = false
                }
            }
        }
    }
}
