package io.github.soulhacler.headersup.game

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Short sounds and vibrations. Uses built-in tones, so the app ships no audio files. */
class Feedback(context: Context) {
    private val tones: ToneGenerator? =
        runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 90) }.getOrNull()

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }

    fun correct() {
        tone(ToneGenerator.TONE_PROP_ACK, 250)
        vibrate(longArrayOf(0, 90))
    }

    fun pass() {
        tone(ToneGenerator.TONE_PROP_NACK, 250)
        vibrate(longArrayOf(0, 40, 70, 40))
    }

    fun tick() = tone(ToneGenerator.TONE_PROP_BEEP, 120)

    fun go() {
        tone(ToneGenerator.TONE_PROP_BEEP2, 300)
        vibrate(longArrayOf(0, 150))
    }

    fun whistle() {
        tone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600)
        vibrate(longArrayOf(0, 300, 120, 300))
    }

    fun release() {
        tones?.release()
    }

    private fun tone(type: Int, durationMs: Int) {
        runCatching { tones?.startTone(type, durationMs) }
    }

    private fun vibrate(pattern: LongArray) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        runCatching { v.vibrate(VibrationEffect.createWaveform(pattern, -1)) }
    }
}
