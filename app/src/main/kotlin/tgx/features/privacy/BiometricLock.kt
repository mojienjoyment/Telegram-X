/*
 * This file is a part of Telegram X Fork
 * Copyright © 2024-2025 (Your Name)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * File created on [DATE]
 */
@file:JvmName("BiometricLock")

package tgx.features.privacy

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import me.vkryl.android.logger.Log
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Biometric authentication manager for app lock functionality.
 * 
 * This class provides a secure way to lock the app using biometric authentication
 * (fingerprint, face unlock, or iris scan) depending on device capabilities.
 * 
 * Features:
 * - Supports fingerprint, face, and iris authentication
 * - Falls back to device PIN/pattern/password if biometrics unavailable
 * - Configurable timeout for re-authentication
 * - Thread-safe implementation
 * 
 * Usage:
 * ```kotlin
 * val biometricLock = BiometricLock(context)
 * 
 * // Check if biometric auth is available
 * if (biometricLock.isBiometricAvailable()) {
 *     biometricLock.authenticate(
 *         title = "Authenticate to open Telegram X",
 *         subtitle = "Use your biometric credential",
 *         onSuccess = {
 *             // Authentication successful
 *         },
 *         onError = { errorCode, errorMessage ->
 *             // Handle error
 *         }
 *     )
 * }
 * ```
 */
class BiometricLock(private val context: Context) {
    
    companion object {
        private const val TAG = "BiometricLock"
        private const val PREFS_NAME = "biometric_lock_prefs"
        private const val KEY_ENABLED = "biometric_enabled"
        private const val KEY_LAST_AUTH = "last_auth_time"
        
        /** Authentication timeout in milliseconds (5 minutes) */
        const val AUTH_TIMEOUT_MS = 5 * 60 * 1000L
    }
    
    private val biometricManager: BiometricManager = BiometricManager.from(context)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val isAuthenticating = AtomicBoolean(false)
    
    /**
     * Biometric authentication capability states
     */
    enum class BiometricState {
        /** Biometric authentication is available and hardware is present */
        AVAILABLE,
        
        /** No biometric hardware present on device */
        HARDWARE_UNAVAILABLE,
        
        /** Hardware present but no biometrics enrolled */
        NO_ENROLLED,
        
        /** Biometric authentication disabled by user/policy */
        DISABLED,
        
        /** Unknown state or error */
        UNKNOWN
    }
    
    /**
     * Check if biometric authentication is enabled in app settings
     */
    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)
    
    /**
     * Enable or disable biometric lock
     */
    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            updateLastAuthTime()
        }
    }
    
    /**
     * Get last successful authentication time
     */
    fun getLastAuthTime(): Long = prefs.getLong(KEY_LAST_AUTH, 0L)
    
    /**
     * Update last authentication timestamp
     */
    private fun updateLastAuthTime() {
        prefs.edit().putLong(KEY_LAST_AUTH, System.currentTimeMillis()).apply()
    }
    
    /**
     * Check if authentication is required based on timeout
     */
    fun needsAuthentication(): Boolean {
        if (!isEnabled()) return false
        
        val lastAuth = getLastAuthTime()
        val elapsed = System.currentTimeMillis() - lastAuth
        
        return elapsed > AUTH_TIMEOUT_MS
    }
    
    /**
     * Check biometric authentication availability
     * 
     * @return [BiometricState] indicating current biometric capability
     */
    fun getBiometricState(): BiometricState {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricState.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricState.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricState.NO_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricState.UNKNOWN
            else -> BiometricState.DISABLED
        }
    }
    
    /**
     * Simplified check if biometric authentication is available
     */
    fun isBiometricAvailable(): Boolean = getBiometricState() == BiometricState.AVAILABLE
    
    /**
     * Check if device has any form of secure lock (biometric or credential)
     */
    fun hasSecureLock(): Boolean {
        val state = getBiometricState()
        return state == BiometricState.AVAILABLE || state == BiometricState.NO_ENROLLED
    }
    
    /**
     * Authenticate using biometrics or device credentials
     * 
     * @param activity FragmentActivity to attach the biometric prompt to
     * @param title Title text for the biometric prompt
     * @param subtitle Subtitle text for the biometric prompt
     * @param description Optional description text
     * @param allowDeviceCredential Allow fallback to device PIN/pattern/password
     * @param onSuccess Callback invoked on successful authentication
     * @param onError Callback invoked on authentication error
     * @param onFailed Callback invoked on authentication failure (wrong fingerprint, etc.)
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String? = null,
        allowDeviceCredential: Boolean = true,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        if (!activity.isFinishing && !activity.isDestroyed) {
            authenticateInternal(
                activity = activity,
                title = title,
                subtitle = subtitle,
                description = description,
                allowDeviceCredential = allowDeviceCredential,
                onSuccess = onSuccess,
                onError = onError,
                onFailed = onFailed
            )
        } else {
            Log.w(TAG, "Cannot authenticate: activity is finishing or destroyed")
            onError(BiometricPrompt.ERROR_CANCELED, "Activity not available")
        }
    }
    
    private fun authenticateInternal(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String?,
        allowDeviceCredential: Boolean,
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit,
        onFailed: () -> Unit
    ) {
        if (!isAuthenticating.compareAndSet(false, true)) {
            Log.w(TAG, "Authentication already in progress")
            return
        }
        
        try {
            val biometricInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setAllowedAuthenticators(
                    if (allowDeviceCredential) {
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    } else {
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                    }
                )
                .build()
            
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.i(TAG, "Biometric authentication succeeded")
                        updateLastAuthTime()
                        onSuccess()
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.w(TAG, "Biometric authentication error: $errorCode - $errString")
                        onError(errorCode, errString.toString())
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.d(TAG, "Biometric authentication failed (wrong credential)")
                        onFailed()
                    }
                }
            )
            
            biometricPrompt.authenticate(biometricInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during biometric authentication", e)
            isAuthenticating.set(false)
            onError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, e.message ?: "Unknown error")
        }
    }
    
    /**
     * Reset authentication timer (mark as recently authenticated)
     */
    fun resetAuthTimer() {
        updateLastAuthTime()
    }
    
    /**
     * Clear all biometric lock settings
     */
    fun clearSettings() {
        prefs.edit().clear().apply()
    }
}
