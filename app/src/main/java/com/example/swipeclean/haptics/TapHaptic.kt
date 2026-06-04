package com.example.swipeclean.haptics

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class UiHaptic(private val view: android.view.View) {
    fun toggle(on: Boolean) {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (on) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.TOGGLE_OFF
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }
        view.performHapticFeedback(constant)
    }

    fun segment() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            HapticFeedbackConstants.SEGMENT_TICK
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }
        view.performHapticFeedback(constant)
    }

    fun slider() {
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            HapticFeedbackConstants.SEGMENT_TICK
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
        view.performHapticFeedback(constant)
    }
    
    fun tap() {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}

@Composable
fun rememberUiHaptic(): UiHaptic {
    val view = LocalView.current
    return remember(view) { UiHaptic(view) }
}

/**
 * Lightweight framework haptics for taps — pairs with [HapticManager] waveforms
 * (gestures keep custom patterns; buttons use [CONTEXT_CLICK]).
 */
@Composable
fun rememberTapHaptic(): () -> Unit {
    val haptic = rememberUiHaptic()
    return remember(haptic) { { haptic.tap() } }
}
