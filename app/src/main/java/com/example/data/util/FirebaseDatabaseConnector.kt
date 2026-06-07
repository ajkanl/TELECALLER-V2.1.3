package com.example.data.util

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object FirebaseDatabaseConnector {
    private const val DEFAULT_RTDB_URL = "https://telecalller-pro-default-rtdb.firebaseio.com"

    fun getInstance(): FirebaseDatabase? {
        // 1. Try standard initialization (reads option 'firebase_url' from loaded google-services.json)
        try {
            return FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            Log.d("FirebaseDBConnector", "Standard dynamic database options fetch bypassed/failed: ${e.message}")
        }

        // 2. Try falling back to project standard endpoint
        try {
            return FirebaseDatabase.getInstance(DEFAULT_RTDB_URL)
        } catch (e: Exception) {
            Log.e("FirebaseDBConnector", "System-defined backup RTDB registration missed: ${e.message}")
        }

        return null
    }
}
