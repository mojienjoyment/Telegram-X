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
@file:JvmName("PreferenceManager")

package tgx.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Modern preference manager using Kotlin delegated properties
 * 
 * This class provides a type-safe and convenient way to work with SharedPreferences
 * using Kotlin's property delegation feature. It supports all common data types
 * and provides both traditional and modern API styles.
 * 
 * Features:
 * - Type-safe property delegation
 * - Support for all primitive types plus Set<String>
 * - Observable preferences with listeners
 * - Transaction support for batch operations
 * - Migration utilities
 * 
 * Usage with delegation:
 * ```kotlin
 * val prefs = PreferenceHelper(context)
 * 
 * // Define preferences as properties
 * var userName: String by prefs.string("user_name", defaultValue = "Guest")
 * var isLoggedIn: Boolean by prefs.boolean("logged_in", defaultValue = false)
 * var messageCount: Int by prefs.int("message_count", defaultValue = 0)
 * 
 * // Use like normal properties
 * userName = "John"
 * if (isLoggedIn) { ... }
 * ```
 * 
 * Traditional usage:
 * ```kotlin
 * prefs.save("key", "value")
 * val value = prefs.getString("key", "default")
 * prefs.remove("key")
 * prefs.clear()
 * ```
 */
class PreferenceHelper(context: Context, private val name: String? = null) {
    
    private val prefs: SharedPreferences = if (name != null) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    } else {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
    
    // ============================================
    // Property Delegates
    // ============================================
    
    /**
     * Delegate for String preferences
     */
    fun string(key: String, defaultValue: String): ReadWriteProperty<Any?, String> {
        return object : ReadWriteProperty<Any?, String> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): String {
                return prefs.getString(key, defaultValue) ?: defaultValue
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                prefs.edit().putString(key, value).apply()
            }
        }
    }
    
    /**
     * Delegate for nullable String preferences
     */
    fun nullableString(key: String): ReadWriteProperty<Any?, String?> {
        return object : ReadWriteProperty<Any?, String?> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
                return prefs.getString(key, null)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
                if (value == null) {
                    prefs.edit().remove(key).apply()
                } else {
                    prefs.edit().putString(key, value).apply()
                }
            }
        }
    }
    
    /**
     * Delegate for Int preferences
     */
    fun int(key: String, defaultValue: Int): ReadWriteProperty<Any?, Int> {
        return object : ReadWriteProperty<Any?, Int> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
                return prefs.getInt(key, defaultValue)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
                prefs.edit().putInt(key, value).apply()
            }
        }
    }
    
    /**
     * Delegate for Long preferences
     */
    fun long(key: String, defaultValue: Long): ReadWriteProperty<Any?, Long> {
        return object : ReadWriteProperty<Any?, Long> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
                return prefs.getLong(key, defaultValue)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
                prefs.edit().putLong(key, value).apply()
            }
        }
    }
    
    /**
     * Delegate for Float preferences
     */
    fun float(key: String, defaultValue: Float): ReadWriteProperty<Any?, Float> {
        return object : ReadWriteProperty<Any?, Float> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
                return prefs.getFloat(key, defaultValue)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
                prefs.edit().putFloat(key, value).apply()
            }
        }
    }
    
    /**
     * Delegate for Boolean preferences
     */
    fun boolean(key: String, defaultValue: Boolean): ReadWriteProperty<Any?, Boolean> {
        return object : ReadWriteProperty<Any?, Boolean> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
                return prefs.getBoolean(key, defaultValue)
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
                prefs.edit().putBoolean(key, value).apply()
            }
        }
    }
    
    /**
     * Delegate for Set<String> preferences
     */
    fun stringSet(key: String, defaultValue: Set<String>): ReadWriteProperty<Any?, Set<String>> {
        return object : ReadWriteProperty<Any?, Set<String>> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Set<String> {
                return prefs.getStringSet(key, defaultValue) ?: defaultValue
            }
            
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<String>) {
                prefs.edit().putStringSet(key, value).apply()
            }
        }
    }
    
    // ============================================
    // Traditional Methods
    // ============================================
    
    /**
     * Save any value by key (auto-detects type)
     */
    fun save(key: String, value: Any?) {
        when (value) {
            is String -> prefs.edit().putString(key, value).apply()
            is Int -> prefs.edit().putInt(key, value).apply()
            is Long -> prefs.edit().putLong(key, value).apply()
            is Float -> prefs.edit().putFloat(key, value).apply()
            is Boolean -> prefs.edit().putBoolean(key, value).apply()
            is Set<*> -> {
                @Suppress("UNCHECKED_CAST")
                prefs.edit().putStringSet(key, value as Set<String>).apply()
            }
            null -> prefs.edit().remove(key).apply()
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
        }
    }
    
    /**
     * Get string value
     */
    fun getString(key: String, defaultValue: String? = null): String? = 
        prefs.getString(key, defaultValue)
    
    /**
     * Get int value
     */
    fun getInt(key: String, defaultValue: Int = 0): Int = 
        prefs.getInt(key, defaultValue)
    
    /**
     * Get long value
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long = 
        prefs.getLong(key, defaultValue)
    
    /**
     * Get float value
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float = 
        prefs.getFloat(key, defaultValue)
    
    /**
     * Get boolean value
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = 
        prefs.getBoolean(key, defaultValue)
    
    /**
     * Get string set value
     */
    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>? = 
        prefs.getStringSet(key, defaultValue)
    
    /**
     * Check if key exists
     */
    fun contains(key: String): Boolean = prefs.contains(key)
    
    /**
     * Remove preference by key
     */
    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
    
    /**
     * Clear all preferences
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Register preference change listener
     */
    fun registerOnChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }
    
    /**
     * Unregister preference change listener
     */
    fun unregisterOnChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
    
    /**
     * Execute batch operations in a single transaction
     */
    inline fun edit(operation: (android.content.SharedPreferences.Editor) -> Unit) {
        val editor = prefs.edit()
        operation(editor)
        editor.apply()
    }
    
    /**
     * Get all preferences
     */
    fun getAll(): Map<String, *> = prefs.all
    
    /**
     * Get SharedPreferences instance directly
     */
    fun getSharedPreferences(): SharedPreferences = prefs
}

/**
 * Quick access preference helpers for common use cases
 */
object Prefs {
    
    private var instance: PreferenceHelper? = null
    
    /**
     * Initialize with application context
     */
    fun init(context: Context, name: String? = null) {
        instance = PreferenceHelper(context.applicationContext, name)
    }
    
    private val prefs: PreferenceHelper
        get() = instance ?: throw IllegalStateException(
            "Prefs not initialized. Call Prefs.init(context) first."
        )
    
    // Delegate shortcuts
    fun string(key: String, default: String) = prefs.string(key, default)
    fun int(key: String, default: Int) = prefs.int(key, default)
    fun long(key: String, default: Long) = prefs.long(key, default)
    fun float(key: String, default: Float) = prefs.float(key, default)
    fun boolean(key: String, default: Boolean) = prefs.boolean(key, default)
    fun stringSet(key: String, default: Set<String>) = prefs.stringSet(key, default)
    
    // Method shortcuts
    fun save(key: String, value: Any?) = prefs.save(key, value)
    fun getString(key: String, default: String? = null) = prefs.getString(key, default)
    fun getInt(key: String, default: Int = 0) = prefs.getInt(key, default)
    fun getLong(key: String, default: Long = 0L) = prefs.getLong(key, default)
    fun getBoolean(key: String, default: Boolean = false) = prefs.getBoolean(key, default)
    fun contains(key: String) = prefs.contains(key)
    fun remove(key: String) = prefs.remove(key)
    fun clear() = prefs.clear()
}

/**
 * Extension function to create PreferenceHelper from Context
 */
fun Context.preferences(name: String? = null): PreferenceHelper = 
    PreferenceHelper(this, name)
