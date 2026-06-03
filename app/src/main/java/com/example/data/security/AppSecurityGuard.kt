package com.example.data.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.File

object AppSecurityGuard {

    /**
     * Performs lightweight system checks looking for test-keys build tags or known Superuser binaries.
     */
    fun isDeviceRooted(context: Context): Boolean {
        try {
            // Check 1: Build tags for custom test-keys
            val buildTags = Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true
            }

            // Check 2: Try finding common Superuser files/binaries
            val commonPaths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )
            for (path in commonPaths) {
                if (File(path).exists()) {
                    return true
                }
            }

            // Check 3: Execute light run command for 'su' presence
            var process: java.lang.Process? = null
            try {
                process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
                val inReader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                if (inReader.readLine() != null) {
                    return true
                }
            } catch (t: Throwable) {
                // Process checking failed or which is not present, ignore
            } finally {
                process?.destroy()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Verifies if the phone is securely connected to our organizational network via TRANSPORT_VPN.
     */
    fun isCorporateVpnActive(context: Context): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
