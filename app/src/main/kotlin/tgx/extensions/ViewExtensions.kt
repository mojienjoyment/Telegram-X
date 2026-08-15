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
@file:JvmName("ViewExtensions")

package tgx.extensions

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Kotlin extension functions for View manipulation
 * 
 * These extensions provide convenient methods for common View operations,
 * reducing boilerplate code and improving readability.
 */

// ============================================
// Visibility Extensions
// ============================================

/**
 * Set view visibility to VISIBLE
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * Set view visibility to GONE
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Set view visibility to INVISIBLE
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Set view visibility based on condition
 * @param visible if true, set to VISIBLE; otherwise GONE
 */
fun View.visibleIf(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * Toggle view visibility between VISIBLE and GONE
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * Check if view is currently visible
 */
fun View.isVisible(): Boolean = visibility == View.VISIBLE

/**
 * Check if view is currently gone
 */
fun View.isGone(): Boolean = visibility == View.GONE

// ============================================
// Click Extensions
// ============================================

/**
 * Set click listener with optional debounce
 * @param debounceTimeMs Time in milliseconds to prevent duplicate clicks (default: 500ms)
 * @param action Click action to perform
 */
fun View.clickWithDebounce(debounceTimeMs: Long = 500L, action: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTimeMs) {
            lastClickTime = currentTime
            action(view)
        }
    }
}

/**
 * Set click listener that requires long press first
 * @param longClickDuration Duration in ms for long press (default: 500ms)
 * @param action Click action to perform
 */
fun View.longPressThenClick(longClickDuration: Long = 500L, action: (View) -> Unit) {
    var isLongPressed = false
    val handler = android.os.Handler(android.os.Looper.getMainLooper())
    
    setOnTouchListener { view, event ->
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                isLongPressed = false
                handler.postDelayed({
                    isLongPressed = true
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                }, longClickDuration)
            }
            android.view.MotionEvent.ACTION_UP,
            android.view.MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacksAndMessages(null)
                if (isLongPressed) {
                    action(view)
                }
            }
        }
        false
    }
}

// ============================================
// Animation Extensions
// ============================================

/**
 * Fade in view with optional duration
 * @param duration Animation duration in milliseconds
 * @param onComplete Callback when animation completes
 */
fun View.fadeIn(duration: Long = 200L, onComplete: (() -> Unit)? = null) {
    val alphaAnimation = AlphaAnimation(0f, 1f).apply {
        this.duration = duration
        fillAfter = true
    }
    
    if (onComplete != null) {
        alphaAnimation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                onComplete()
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
    }
    
    startAnimation(alphaAnimation)
    visible()
}

/**
 * Fade out view with optional duration
 * @param duration Animation duration in milliseconds
 * @param onComplete Callback when animation completes
 */
fun View.fadeOut(duration: Long = 200L, onComplete: (() -> Unit)? = null) {
    val alphaAnimation = AlphaAnimation(1f, 0f).apply {
        this.duration = duration
        fillAfter = true
    }
    
    if (onComplete != null) {
        alphaAnimation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                onComplete()
                gone()
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
    } else {
        alphaAnimation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                gone()
            }
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
    }
    
    startAnimation(alphaAnimation)
}

/**
 * Pulse animation effect
 * @param duration Duration of one pulse cycle
 * @param repeatCount Number of times to repeat (-1 for infinite)
 */
fun View.pulse(duration: Long = 1000L, repeatCount: Int = 1) {
    val scaleAnimation = android.view.animation.ScaleAnimation(
        1f, 1.1f, 1f, 1.1f,
        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        this.duration = duration / 2
        repeatMode = android.view.animation.Animation.REVERSE
        this.repeatCount = repeatCount * 2 - 1
        interpolator = android.view.animation.DecelerateInterpolator()
    }
    
    startAnimation(scaleAnimation)
}

// ============================================
// Background Extensions
// ============================================

/**
 * Set background color from color resource
 * @param colorRes Color resource ID
 */
fun View.setBackgroundFromResource(@DrawableRes colorRes: Int) {
    setBackgroundColor(ContextCompat.getColor(context, colorRes))
}

/**
 * Set background with ripple effect for touch feedback
 */
fun View.enableRipple() {
    val attrs = intArrayOf(android.R.attr.selectableItemBackground)
    val typedArray = context.obtainStyledAttributes(attrs)
    val background = typedArray.getDrawable(0)
    typedArray.recycle()
    setBackground(background)
}

// ============================================
// Padding & Margin Extensions
// ============================================

/**
 * Set padding in DP instead of pixels
 */
fun View.setPaddingDp(left: Int, top: Int, right: Int, bottom: Int) {
    val density = context.resources.displayMetrics.density
    setPadding(
        (left * density).toInt(),
        (top * density).toInt(),
        (right * density).toInt(),
        (bottom * density).toInt()
    )
}

/**
 * Set uniform padding in DP
 */
fun View.setPaddingDp(all: Int) {
    setPaddingDp(all, all, all, all)
}

/**
 * Remove all padding
 */
fun View.clearPadding() {
    setPadding(0, 0, 0, 0)
}

// ============================================
// Layout Extensions
// ============================================

/**
 * Get view width after layout
 * @param callback Function to receive width value
 */
fun View.onGlobalLayout(callback: (width: Int, height: Int) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener {
        callback(width, height)
        // Remove listener to prevent memory leaks
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeOnGlobalLayoutListener(this::onGlobalLayout)
        } else {
            @Suppress("DEPRECATION")
            viewTreeObserver.removeGlobalOnLayoutListener(this::onGlobalLayout)
        }
    }
}

/**
 * Check if view is laid out and has valid dimensions
 */
fun View.isLaidOut(): Boolean = width > 0 && height > 0

// ============================================
// Context Extensions (for Views)
// ============================================

/**
 * Get string resource
 */
fun View.getString(resId: Int): String = context.getString(resId)

/**
 * Get formatted string resource
 */
fun View.getString(resId: Int, vararg formatArgs: Any): String = 
    context.getString(resId, *formatArgs)

/**
 * Get color resource
 */
fun View.getColor(resId: Int): Int = ContextCompat.getColor(context, resId)

/**
 * Get drawable resource
 */
fun View.getDrawable(resId: Int): android.graphics.drawable.Drawable? = 
    ContextCompat.getDrawable(context, resId)

// ============================================
// Utility Extensions
// ============================================

/**
 * Make view clickable without performing any action
 * Useful for views that need to appear clickable but don't have specific actions
 */
fun View.makeClickable() {
    isClickable = true
    isFocusable = true
    enableRipple()
}

/**
 * Disable view interaction completely
 */
fun View.disableInteraction() {
    isEnabled = false
    isClickable = false
    isFocusable = false
    alpha = 0.5f
}

/**
 * Enable view interaction
 */
fun View.enableInteraction() {
    isEnabled = true
    isClickable = true
    isFocusable = true
    alpha = 1.0f
}

/**
 * Execute action when view is attached to window
 */
fun View.whenAttached(action: () -> Unit) {
    if (isAttachedToWindow) {
        action()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                action()
                removeOnAttachStateChangeListener(this)
            }
            
            override fun onViewDetachedFromWindow(v: View) {}
        })
    }
}
