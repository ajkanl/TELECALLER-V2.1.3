package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.DebtorDao
import com.example.data.util.DataImportHandler
import com.example.domain.model.CallRecord
import com.example.domain.model.Debtor
import com.example.domain.repository.DebtorRepository
import com.example.domain.security.SecuritySettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DebtorRepository,
    private val securitySettingsStore: SecuritySettingsStore,
    private val debtorDao: DebtorDao,
    private val collectionDao: com.example.data.local.dao.CollectionDao,
    private val geminiRepository: com.example.domain.repository.GeminiRepository
) : ViewModel() {

    // --- AI SCRIPT AND LOG NOTES ENHANCEMENT STATES ---
    private val _aiScriptState = MutableStateFlow<String?>(null)
    val aiScriptState: StateFlow<String?> = _aiScriptState.asStateFlow()

    private val _isGeneratingScript = MutableStateFlow(false)
    val isGeneratingScript: StateFlow<Boolean> = _isGeneratingScript.asStateFlow()

    private val _aiOptimizedNotesState = MutableStateFlow<String?>(null)
    val aiOptimizedNotesState: StateFlow<String?> = _aiOptimizedNotesState.asStateFlow()

    private val _isOptimizingNotes = MutableStateFlow(false)
    val isOptimizingNotes: StateFlow<Boolean> = _isOptimizingNotes.asStateFlow()

    private val _aiPortfolioAnalysisState = MutableStateFlow<String?>(null)
    val aiPortfolioAnalysisState: StateFlow<String?> = _aiPortfolioAnalysisState.asStateFlow()

    private val _isGeneratingPortfolioAnalysis = MutableStateFlow(false)
    val isGeneratingPortfolioAnalysis: StateFlow<Boolean> = _isGeneratingPortfolioAnalysis.asStateFlow()

    fun generatePortfolioAnalysis(
        debtorsCount: Int,
        totalOutstanding: Double,
        activePtpCount: Int,
        recentLogsSummary: String
    ) {
        viewModelScope.launch {
            _isGeneratingPortfolioAnalysis.value = true
            try {
                val analysis = geminiRepository.generatePortfolioExecutiveAnalysis(
                    debtorsCount = debtorsCount,
                    totalOutstanding = totalOutstanding,
                    activePtpCount = activePtpCount,
                    recentLogsSummary = recentLogsSummary
                )
                _aiPortfolioAnalysisState.value = analysis
            } catch (e: Exception) {
                _aiPortfolioAnalysisState.value = "Failed to generate report: ${e.localizedMessage}"
            } finally {
                _isGeneratingPortfolioAnalysis.value = false
            }
        }
    }

    fun clearPortfolioAnalysis() {
        _aiPortfolioAnalysisState.value = null
    }

    fun generateNegotiationScript(
        studentName: String,
        amount: Double,
        college: String,
        segment: String,
        previousNotes: String
    ) {
        viewModelScope.launch {
            _isGeneratingScript.value = true
            try {
                val script = geminiRepository.generateNegotiationScript(
                    studentName, amount, college, segment, previousNotes
                )
                _aiScriptState.value = script
            } catch (e: Exception) {
                _aiScriptState.value = "Failed to generate script: ${e.localizedMessage}"
            } finally {
                _isGeneratingScript.value = false
            }
        }
    }

    fun optimizeCallNotes(rawNotes: String) {
        viewModelScope.launch {
            _isOptimizingNotes.value = true
            try {
                val optimized = geminiRepository.optimizeCallNotes(rawNotes)
                _aiOptimizedNotesState.value = optimized
            } catch (e: Exception) {
                _aiOptimizedNotesState.value = "Failed to optimize notes: ${e.localizedMessage}"
            } finally {
                _isOptimizingNotes.value = false
            }
        }
    }

    fun clearScript() {
        _aiScriptState.value = null
    }

    fun clearOptimizedNotes() {
        _aiOptimizedNotesState.value = null
    }

    fun getPromisesForDebtor(debtorId: String) = collectionDao.getAllPromisesToPay().map { list ->
        list.filter { it.debtorId == debtorId }
    }

    fun importStudentDataCollegeWise(
        csvContent: String,
        collegeName: String,
        isStudentDbSchema: Boolean = false,
        onFinished: (Int, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val handler = DataImportHandler(debtorDao)
                val result = handler.parseAndValidateCsvText(csvContent, isStudentDbSchema)
                if (result.successfullyImported.isNotEmpty()) {
                    val count = handler.saveToLocalDatabase(result.successfullyImported, collegeName)
                    val errorString = if (result.errorLogs.isNotEmpty()) {
                        "Successfully imported $count rows. Some rows had errors:\n" + result.errorLogs.joinToString("\n")
                    } else {
                        null
                    }
                    onFinished(count, errorString)
                } else {
                    val errorString = if (result.errorLogs.isNotEmpty()) {
                        result.errorLogs.joinToString("\n")
                    } else {
                        "No student rows found to import. Please check format."
                    }
                    onFinished(0, errorString)
                }
            } catch (e: Exception) {
                onFinished(0, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun maskPhoneNumber(number: String): String {
        return securitySettingsStore.maskPhoneNumber(number)
    }

    val isNumberMaskingEnabled: StateFlow<Boolean> = securitySettingsStore.isNumberMaskingEnabled

    val telecallersList = securitySettingsStore.telecallersList

    val activeImpersonatedAgent: StateFlow<com.example.domain.security.TelecallerAgent?> = securitySettingsStore.activeImpersonatedAgent

    fun updateAgentProfile(agentId: String, name: String, profilePicture: String?) {
        securitySettingsStore.updateAgentProfile(agentId, name, profilePicture)
    }

    // Expose flows from the domain repository
    val debtors = repository.getDebtors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentCalls = repository.getRecentCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyCallProgress = repository.getDailyCallProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(42, 60))

    val dailyRecoveredAmount = repository.getDailyRecoveredAmount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120000.0)

    // Sim Card controls
    private val _currentSim = MutableStateFlow("SIM 1")
    val currentSim: StateFlow<String> = _currentSim.asStateFlow()

    // Simulated Active Call State
    private val _activeCallDebtor = MutableStateFlow<Debtor?>(null)
    val activeCallDebtor: StateFlow<Debtor?> = _activeCallDebtor.asStateFlow()

    private val _isDialing = MutableStateFlow(false)
    val isDialing: StateFlow<Boolean> = _isDialing.asStateFlow()

    // Profile detail & persistent edit state
    private val _selectedDebtor = MutableStateFlow<Debtor?>(null)
    val selectedDebtor: StateFlow<Debtor?> = _selectedDebtor.asStateFlow()

    // Call Disposition Flow State
    private val _dispositionDebtor = MutableStateFlow<Debtor?>(null)
    val dispositionDebtor: StateFlow<Debtor?> = _dispositionDebtor.asStateFlow()

    private val _dialerEvents = MutableSharedFlow<String>()
    val dialerEvents: SharedFlow<String> = _dialerEvents.asSharedFlow()

    fun selectDispositionDebtor(debtor: Debtor?) {
        _dispositionDebtor.value = debtor
    }

    fun switchSim() {
        _currentSim.value = if (_currentSim.value == "SIM 1") "SIM 2" else "SIM 1"
    }

    fun initiateCall(debtor: Debtor) {
        viewModelScope.launch {
            _activeCallDebtor.value = debtor
            _isDialing.value = true
            _dialerEvents.emit(debtor.phoneNumber)
        }
    }

    fun endCallWithResult(status: String) {
        viewModelScope.launch {
            val debtor = _activeCallDebtor.value
            if (debtor != null) {
                repository.recordCall(
                    debtorName = debtor.name,
                    status = status,
                    simCard = _currentSim.value
                )
                _dispositionDebtor.value = debtor // Open the disposition screen for this debtor automatically!
            }
            _isDialing.value = false
            _activeCallDebtor.value = null
        }
    }

    fun cancelCall() {
        _isDialing.value = false
        _activeCallDebtor.value = null
    }

    // Debtor selection & edit routines
    fun selectDebtor(debtor: Debtor?) {
        _selectedDebtor.value = debtor
    }

    fun updateDebtorProfile(updated: Debtor) {
        viewModelScope.launch {
            repository.updateDebtor(updated)
            if (_selectedDebtor.value?.id == updated.id) {
                _selectedDebtor.value = updated
            }
        }
    }

    fun getCallLogsForDebtor(debtorId: String) = repository.getCallLogsForDebtor(debtorId)

    fun addCallLogEntry(
        debtorId: String,
        debtorName: String,
        outcome: String,
        notes: String,
        ptpDate: String? = null,
        ptpAmount: Double? = null
    ) {
        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val activeAgent = securitySettingsStore.activeImpersonatedAgent.value
            val agentId = activeAgent?.id ?: "T01"
            val agentName = activeAgent?.name ?: (securitySettingsStore.telecallersList.value.firstOrNull { it.id == "T01" }?.name ?: "Rajesh Kumar")
            repository.saveCallLog(
                debtorId = debtorId,
                debtorName = debtorName,
                date = currentDate,
                time = currentTime,
                outcome = outcome,
                notes = notes,
                agentId = agentId,
                agentName = agentName,
                ptpDate = ptpDate,
                ptpAmount = ptpAmount
            )
            val updatedDebtor = _selectedDebtor.value?.copy(lastContactDate = "0 Days Ago")
            if (updatedDebtor != null) {
                _selectedDebtor.value = updatedDebtor
            }
        }
    }
}
