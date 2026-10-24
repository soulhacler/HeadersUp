package io.github.soulhacler.headersup.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

enum class Tilt { DOWN, UP }

/**
 * Detects forward/backward tilts while the phone is held against the forehead.
 *
 * The sensor Z axis points out of the screen. Upright on a forehead the screen faces
 * forward, so gravity along Z is close to zero. Tipping the top of the phone down
 * (screen towards the floor) drives Z negative; tipping it up (screen towards the
 * ceiling) drives it positive. Because only Z is used, this works in either landscape
 * rotation.
 *
 * After every tilt, and at the start, the phone must return to upright for a moment
 * before another tilt counts. That stops one movement scoring twice, and stops a phone
 * lying flat on a table from passing straight away.
 */
class TiltDetector(context: Context, private val onTilt: (Tilt) -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val sensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val needsFiltering = sensor?.type == Sensor.TYPE_ACCELEROMETER

    val isAvailable: Boolean get() = sensor != null

    private var armed = false
    private var neutralSinceMs = 0L
    private var filtered = FloatArray(3)
    private var hasFiltered = false

    fun start() {
        val s = sensor ?: return
        armed = false
        neutralSinceMs = 0L
        hasFiltered = false
        sensorManager?.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val values = if (needsFiltering) lowPass(event.values) else event.values
        val (x, y, z) = Triple(values[0], values[1], values[2])
        val g = sqrt(x * x + y * y + z * z)
        if (g < 1f) return
        val zNorm = z / g
        val nowMs = event.timestamp / 1_000_000L

        if (!armed) {
            if (abs(zNorm) < NEUTRAL_LIMIT) {
                if (neutralSinceMs == 0L) neutralSinceMs = nowMs
                if (nowMs - neutralSinceMs >= NEUTRAL_HOLD_MS) armed = true
            } else {
                neutralSinceMs = 0L
            }
            return
        }

        val tilt = when {
            zNorm < -TRIGGER_LIMIT -> Tilt.DOWN
            zNorm > TRIGGER_LIMIT -> Tilt.UP
            else -> null
        } ?: return
        armed = false
        neutralSinceMs = 0L
        onTilt(tilt)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun lowPass(input: FloatArray): FloatArray {
        if (!hasFiltered) {
            filtered = input.copyOf(3)
            hasFiltered = true
        } else {
            for (i in 0..2) filtered[i] = ALPHA * filtered[i] + (1 - ALPHA) * input[i]
        }
        return filtered
    }

    private companion object {
        /** sin(~44°): how far the screen must face the floor/ceiling to count. */
        const val TRIGGER_LIMIT = 0.7f
        /** sin(~25°): how close to upright counts as "back on the forehead". */
        const val NEUTRAL_LIMIT = 0.42f
        const val NEUTRAL_HOLD_MS = 200L
        const val ALPHA = 0.8f
    }
}
