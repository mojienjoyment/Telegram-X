/*
 * This file is a part of Telegram X
 * Copyright © 2014 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package tgx.features.privacy

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import org.thunderdog.challegram.R

/**
 * Screenshot prevention feature that blocks screenshots and screen recording.
 * Uses FLAG_SECURE to prevent content from appearing in screenshots or screen recordings.
 * 
 * This is compliant with Telegram TOS as it's a privacy enhancement for users.
 * 
 * Usage:
 * ```kotlin
 * val screenshotPrevention = ScreenshotPrevention(context)
 * 
 * // Enable in Activity onCreate
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     screenshotPrevention.enable(this)
 * }
 * 
 * // Or disable temporarily
 * screenshotPrevention.disable(this)
 * ```
 */
class ScreenshotPrevention(private val context: Context) {
    
    private var isProximitySensorAvailable = false
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var proximityEventListener: SensorEventListener? = null
    
    /**
     * Enable screenshot and screen recording prevention for the given activity.
     * This will make the window secure, preventing:
     * - Screenshots
     * - Screen recordings
     * - Content visibility in recent apps
     * - Content on non-secure displays
     * 
     * @param activity The activity to protect
     */
    fun enable(activity: Activity) {
        activity.window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        
        // Optional: Enable proximity sensor detection for additional privacy
        // initProximitySensor(activity)
    }
    
    /**
     * Disable screenshot prevention (use with caution).
     * Only call this if you need to allow screenshots temporarily.
     * 
     * @param activity The activity to unprotect
     */
    fun disable(activity: Activity) {
        activity.window.clearFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
    }
    
    /**
     * Check if screenshot prevention is currently enabled for an activity.
     * 
     * @param activity The activity to check
     * @return true if FLAG_SECURE is set
     */
    fun isEnabled(activity: Activity): Boolean {
        return activity.window.attributes.flags and 
            android.view.WindowManager.LayoutParams.FLAG_SECURE != 0
    }
    
    /**
     * Initialize proximity sensor detection (optional advanced feature).
     * Can be used to detect when phone is near ear during calls.
     * 
     * @param activity The activity context
     */
    private fun initProximitySensor(activity: Activity) {
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        
        if (proximitySensor != null) {
            isProximitySensorAvailable = true
            
            proximityEventListener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        val distance = it.values[0]
                        // Distance < 1.0 cm means phone is near ear
                        if (distance < 1.0f) {
                            // Phone is near ear - could disable screen or other actions
                            // This is optional and depends on your use case
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Start listening to proximity sensor events.
     */
    fun startProximityListening(activity: Activity) {
        if (isProximitySensorAvailable) {
            proximityEventListener?.let { listener ->
                sensorManager?.registerListener(
                    listener,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
    }
    
    /**
     * Stop listening to proximity sensor events.
     * Call this in Activity.onPause() or onDestroy() to save battery.
     */
    fun stopProximityListening() {
        proximityEventListener?.let { listener ->
            sensorManager?.unregisterListener(listener)
        }
    }
    
    /**
     * Clean up resources. Call this when the feature is no longer needed.
     */
    fun destroy() {
        stopProximityListening()
        sensorManager = null
        proximitySensor = null
        proximityEventListener = null
    }
}
