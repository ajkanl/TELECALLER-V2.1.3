package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.CollectionDao
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.PromiseToPayEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CallUiState {
    object Idle : CallUiState
    data class ActiveCall(val debtorDetail: DebtorEntity) : CallUiState
    data class WrapUpDisposition(val debtorDetail: DebtorEntity) : CallUiState
}

@HiltViewModel
class CallWorkflowViewModel @Inject constructor(
    private val collectionDao: CollectionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<CallUiState>(CallUiState.Idle)
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    /**
     * Triggered on call startup: queries database for matching debtor and transitions to ActiveCall.
     */
    fun onCallStarted(phoneNumber: String) {
        viewModelScope.launch {
            val debtor = collectionDao.getDebtorByNumberImmediate(phoneNumber) ?: DebtorEntity(
                id = "unknown_${System.currentTimeMillis()}",
                name = "Unknown Caller",
                phoneNumber = phoneNumber,
                alternativeNumber = null,
                totalOverdueAmount = 0.0,
                principalAmount = 0.0,
                dpdBucket = "0 DPD",
                allocationDate = System.currentTimeMillis(),
                currentStatus = "PENDING"
            )
            _uiState.value = CallUiState.ActiveCall(debtor)
        }
    }

    /**
     * Triggered on call disconnect: transitions to WrapUpDisposition, revealing the input logging UI.
     */
    fun onCallEnded() {
        val currentState = _uiState.value
        if (currentState is CallUiState.ActiveCall) {
            _uiState.value = CallUiState.WrapUpDisposition(currentState.debtorDetail)
        }
    }

    /**
     * Commits disposition logs and (optionally) PTP objects to Room database persistence, and resets status to Idle.
     */
    fun submitDisposition(log: CallLogEntity, ptp: PromiseToPayEntity?) {
        viewModelScope.launch {
            try {
                collectionDao.insertCallLog(log)
                ptp?.let {
                    collectionDao.insertOrUpdatePromiseToPay(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = CallUiState.Idle
            }
        }
    }
}
