package com.example.data.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object CallEndTracker {
    data class CallEvent(val phoneNumber: String)

    private val _callEndedEvents = MutableSharedFlow<CallEvent>(extraBufferCapacity = 10)
    val callEndedEvents = _callEndedEvents.asSharedFlow()

    fun emitCallEnded(phoneNumber: String) {
        _callEndedEvents.tryEmit(CallEvent(phoneNumber))
    }
}
