package com.example.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

/**
 * BroadcastReceiver responsible for monitoring telephone connection hooks, ringing sequences,
 * and idle events during debt collections, safely ignoring state repetitions using a companion cache.
 */
class CollectionCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        try {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = try {
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            } catch (e: SecurityException) {
                "Restricted"
            }

            Log.d(TAG, "Telephony event broadcast received. State: $stateStr, Phone Number: $incomingNumber")

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    lastState = STATE_RINGING
                    Log.d(TAG, "Call is ringing. Incoming number: $incomingNumber")
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Prevent duplicate trigger execution
                    if (lastState != STATE_OFFHOOK) {
                        lastState = STATE_OFFHOOK
                        Log.d(TAG, "Call connects (offhook). Initiating operational recording workflows.")
                        
                        // START_RECORDING_SERVICE
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Prevent duplicate trigger execution on return to idle state
                    if (lastState != STATE_IDLE) {
                        lastState = STATE_IDLE
                        Log.d(TAG, "Call disconnects (idle). Triggering disposition screens.")
                        
                        // TRIGGER_DISPOSITION_SCREEN
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing phone state changed intent (likely missing READ_PHONE_STATE permission)", e)
        }
    }

    companion object {
        private const val TAG = "CollectionCallReceiver"

        private const val STATE_IDLE = 0
        private const val STATE_RINGING = 1
        private const val STATE_OFFHOOK = 2

        // Keep local memory to protect again double broadcasts for SIM/sub-ID streams
        private var lastState = STATE_IDLE
    }
}
