package io.github.soulhacler.headersup.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

enum class Tilt { DOWN, UP }

/**
 * Detects forward/backward tilts while the phone is held against the forehead.
 *
 * The sensor Z axis points out of the screen, so the phone's pitch is the angle between
 * the screen and vertical: 0° upright, negative with the screen towards the floor
 * (tilt down), positive with the screen towards the ceiling (tilt up). Only Z is used,
 * so this works in either landscape rotation.
 *
 * People hold the phone at all sorts of angles, so tilts are measured relative to a
 * baseline: the pitch once the phone is held steady at the start of the round. The
 * baseline slowly follows the player's head while they are not tilting.
 *
 * After every tilt the phone must come back near the baseline before another tilt
 * counts, so one movement can't score twice.
 */
class TiltDetector(context: Context, private val onTilt: (Tilt) -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val sensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isAvailable: Boolean get() = sensor != null

    private var smoothedPitch: Float? = null
    private var baseline: Float? = null
    private var armed = false

    // Calibration: wait until the pitch stays within a small window for a moment.
    private var steadySinceMs = 0L
    private var steadyMin = 0f
    private var steadyMax = 0f
    private var steadySum = 0f
    private var steadyCount = 0

    fun start() {
        val s = sensor ?: return
        smoothedPitch = null
        baseline = null
        armed = false
        steadySinceMs = 0L
        sensorManager?.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val horizontal = sqrt(x * x + y * y)
        if (horizontal == 0f && z == 0f) return
        val rawPitch = Math.toDegrees(atan2(z, horizontal).toDouble()).toFloat()
        val pitch = smoothedPitch?.let { it + SMOOTHING * (rawPitch - it) } ?: rawPitch
        smoothedPitch = pitch
        val nowMs = event.timestamp / 1_000_000L

        val base = baseline
        if (base == null) {
            calibrate(pitch, nowMs)
            return
        }

        val delta = pitch - base
        if (!armed) {
            if (abs(delta) < REARM_DEGREES) armed = true
            return
        }
        when {
            delta <= -TRIGGER_DEGREES -> fire(Tilt.DOWN)
            delta >= TRIGGER_DEGREES -> fire(Tilt.UP)
            abs(delta) < REARM_DEGREES -> baseline = base + DRIFT * delta
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun calibrate(pitch: Float, nowMs: Long) {
        if (steadySinceMs == 0L || pitch < steadyMax - STEADY_DEGREES || pitch > steadyMin + STEADY_DEGREES) {
            steadySinceMs = nowMs
            steadyMin = pitch
            steadyMax = pitch
            steadySum = 0f
            steadyCount = 0
        }
        steadyMin = minOf(steadyMin, pitch)
        steadyMax = maxOf(steadyMax, pitch)
        steadySum += pitch
        steadyCount++
        if (nowMs - steadySinceMs >= STEADY_MS) {
            baseline = (steadySum / steadyCount).coerceIn(-MAX_BASELINE_DEGREES, MAX_BASELINE_DEGREES)
            armed = true
        }
    }

    private fun fire(tilt: Tilt) {
        armed = false
        onTilt(tilt)
    }

    private companion object {
        /** How far from the baseline a tilt has to go to count. */
        const val TRIGGER_DEGREES = 30f
        /** How close to the baseline counts as "back on the forehead". */
        const val REARM_DEGREES = 15f
        /** Keeps the baseline where both a tilt up and a tilt down are still possible. */
        const val MAX_BASELINE_DEGREES = 50f
        const val STEADY_DEGREES = 8f
        const val STEADY_MS = 300L
        /** Low-pass filter on the pitch, to ignore jolts from walking or laughing. */
        const val SMOOTHING = 0.25f
        /** How quickly the baseline follows slow head movement. */
        const val DRIFT = 0.02f
    }
}
