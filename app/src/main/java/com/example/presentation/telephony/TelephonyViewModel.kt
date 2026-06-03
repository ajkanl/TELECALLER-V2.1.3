package com.example.presentation.telephony

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelephonySettingsStore @Inject constructor() {
    private val _simRule = MutableStateFlow("Allow Agent Choice (Default)")
    val simRule: StateFlow<String> = _simRule.asStateFlow()

    private val _recordingPolicy = MutableStateFlow("Record All Interactions (Mandatory)")
    val recordingPolicy: StateFlow<Boolean> = MutableStateFlow(true) // placeholder-compatible
    val recordingPolicyString: StateFlow<String> = _recordingPolicy.asStateFlow()

    private val _maxDailyCalls = MutableStateFlow(150)
    val maxDailyCalls: StateFlow<Int> = _maxDailyCalls.asStateFlow()

    fun updateSimRule(rule: String) {
        _simRule.value = rule
    }

    fun updateRecordingPolicy(policy: String) {
        _recordingPolicy.value = policy
    }

    fun updateMaxDailyCalls(limit: Int) {
        _maxDailyCalls.value = limit.coerceIn(50, 500)
    }
}

@HiltViewModel
class TelephonyViewModel @Inject constructor(
    private val store: TelephonySettingsStore
) : ViewModel() {

    val simRule: StateFlow<String> = store.simRule
    val recordingPolicy: StateFlow<String> = store.recordingPolicyString
    val maxDailyCalls: StateFlow<Int> = store.maxDailyCalls

    fun updateSimRule(rule: String) {
        store.updateSimRule(rule)
    }

    fun updateRecordingPolicy(policy: String) {
        store.updateRecordingPolicy(policy)
    }

    fun updateMaxDailyCalls(limit: Int) {
        store.updateMaxDailyCalls(limit)
    }
}
