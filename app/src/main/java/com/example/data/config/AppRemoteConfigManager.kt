package com.example.data.config

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRemoteConfigManager(
    private val remoteConfig: FirebaseRemoteConfig?
) {

    @Inject
    constructor(@ApplicationContext context: Context) : this(
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseRemoteConfig.getInstance()
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    )

    // For testing / backward compatibility
    constructor() : this(null)

    private val _cooldownDaysStandard = MutableStateFlow(15)
    val cooldownDaysStandardFlow: StateFlow<Int> = _cooldownDaysStandard.asStateFlow()

    private val _cooldownDaysTimeline = MutableStateFlow(15)
    val cooldownDaysTimelineFlow: StateFlow<Int> = _cooldownDaysTimeline.asStateFlow()

    private val _maxDailyCallThrottle = MutableStateFlow(250)
    val maxDailyCallThrottleFlow: StateFlow<Int> = _maxDailyCallThrottle.asStateFlow()

    private val _forceUpdateVersionCode = MutableStateFlow(1)
    val forceUpdateVersionCodeFlow: StateFlow<Int> = _forceUpdateVersionCode.asStateFlow()

    private val _configUpdated = MutableStateFlow(0)
    val configUpdated: StateFlow<Int> = _configUpdated.asStateFlow()

    init {
        setupRemoteConfig()
    }

    private fun setupRemoteConfig() {
        try {
            val config = remoteConfig ?: return
            // Set minimum fetch interval to 1 hour (3600 seconds)
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            config.setConfigSettingsAsync(configSettings)

            // Define safe local fallback defaults
            val defaults = mapOf(
                "cooldown_days_standard" to 15L,
                "cooldown_days_timeline" to 15L,
                "max_daily_call_throttle" to 250L,
                "force_update_version_code" to 1L
            )
            config.setDefaultsAsync(defaults)

            // Fetch and activate config parameters
            config.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("RemoteConfig", "Fetch and activate succeeded.")
                        updateProperties()
                    } else {
                        Log.w("RemoteConfig", "Fetch failed, using defaults or cached parameters")
                        updateProperties()
                    }
                }
        } catch (e: Exception) {
            Log.e("RemoteConfig", "Error setting up Remote Config: ", e)
            updateProperties() // ensure fallback
        }
    }

    /**
     * Updates exposed flow streams with fresh values from Remote Config.
     */
    private fun updateProperties() {
        try {
            val config = remoteConfig
            if (config != null) {
                _cooldownDaysStandard.value = config.getLong("cooldown_days_standard").toInt()
                _cooldownDaysTimeline.value = config.getLong("cooldown_days_timeline").toInt()
                _maxDailyCallThrottle.value = config.getLong("max_daily_call_throttle").toInt()
                _forceUpdateVersionCode.value = config.getLong("force_update_version_code").toInt()
            }
            _configUpdated.value += 1
        } catch (e: Exception) {
            Log.e("RemoteConfig", "Error reading config parameters: ", e)
        }
    }

    // Dynamic property accessors
    val cooldownDaysStandard: Int
        get() = remoteConfig?.getLong("cooldown_days_standard")?.toInt() ?: 15

    val cooldownDaysTimeline: Int
        get() = remoteConfig?.getLong("cooldown_days_timeline")?.toInt() ?: 15

    val maxDailyCallThrottle: Int
        get() = remoteConfig?.getLong("max_daily_call_throttle")?.toInt() ?: 250

    val forceUpdateVersionCode: Int
        get() = remoteConfig?.getLong("force_update_version_code")?.toInt() ?: 1
}
