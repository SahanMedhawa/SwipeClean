package com.example.swipeclean.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibrationAttributes
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.example.swipeclean.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Custom waveform haptics for swipe gestures and milestones.
 *
 * Respects:
 * - In-app [AppSettings.hapticsEnabled] and intensity
 * - System **Touch feedback** ([Settings.System.HAPTIC_FEEDBACK_ENABLED])
 * - **Reduced motion** via [Settings.Global.TRANSITION_ANIMATION_SCALE] — softer / shorter pulses
 *
 * On API 33+, uses [VibrationAttributes.USAGE_TOUCH] so OEM power managers classify
 * feedback correctly.
 */
@Singleton
class HapticManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val vibrator: Vibrator? = run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    @Volatile private var settings: AppSettings = AppSettings()

    fun updateSettings(snapshot: AppSettings) {
        settings = snapshot
    }

    fun crossThresholdTick() = play(longArrayOf(0, 10), AMPLITUDE_SOFT)
    fun snapBackBump() = play(longArrayOf(0, 18, 28, 10), AMPLITUDE_SOFT)
    fun binThud() = play(longArrayOf(0, 14, 22, 38), AMPLITUDE_STRONG)
    fun keepConfirmation() = play(longArrayOf(0, 20), AMPLITUDE_SOFT)
    fun favoriteDoublePulse() = play(longArrayOf(0, 18, 60, 28), AMPLITUDE_MEDIUM)
    fun binEmptied() = play(longArrayOf(0, 60, 40, 80, 30, 120), AMPLITUDE_STRONG)
    fun undoReverseTick() = play(longArrayOf(0, 12, 40, 8), AMPLITUDE_SOFT)

    private fun play(timings: LongArray, baseAmplitude: Int) {
        if (!settings.hapticsEnabled) return
        if (!systemTouchHapticsEnabled()) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        val amplitudeScale = if (isReducedMotion()) REDUCED_MOTION_INTENSITY else 1f
        val scaled = (baseAmplitude * settings.hapticIntensity.scale * amplitudeScale)
            .roundToInt()
            .coerceIn(1, 255)

        val effectiveTimings = if (isReducedMotion()) {
            timings.map { t -> (t * REDUCED_MOTION_TIME).roundToInt().coerceAtLeast(1).toLong() }.toLongArray()
        } else {
            timings
        }

        val amplitudes = IntArray(effectiveTimings.size) { idx ->
            if (idx == 0 || idx % 2 == 0) 0 else scaled
        }

        val effect = VibrationEffect.createWaveform(effectiveTimings, amplitudes, -1)
        vibrateWithAttributes(v, effect)
    }

    private fun vibrateWithAttributes(vibrator: Vibrator, effect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attrs = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_TOUCH)
                .build()
            vibrator.vibrate(effect, attrs)
        } else {
            vibrator.vibrate(effect)
        }
    }

    private fun systemTouchHapticsEnabled(): Boolean {
        return try {
            Settings.System.getInt(
                appContext.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1
            ) == 1
        } catch (_: Throwable) {
            true
        }
    }

    /**
     * When system transition animations are off, treat as reduced-motion for **custom**
     * vibration patterns (shorter / softer).
     */
    private fun isReducedMotion(): Boolean {
        return try {
            Settings.Global.getFloat(
                appContext.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            ) == 0f
        } catch (_: Throwable) {
            false
        }
    }

    private companion object {
        const val AMPLITUDE_SOFT = 80
        const val AMPLITUDE_MEDIUM = 160
        const val AMPLITUDE_STRONG = 230
        const val REDUCED_MOTION_INTENSITY = 0.55f
        const val REDUCED_MOTION_TIME = 0.65f
    }
}
