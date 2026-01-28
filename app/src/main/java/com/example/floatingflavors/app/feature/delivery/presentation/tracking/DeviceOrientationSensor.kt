package com.example.floatingflavors.app.feature.delivery.presentation.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Listens to physical device rotation (Compass)
 * Used to rotate the marker when the driver is stationary.
 */
class DeviceOrientationSensor(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    // Prefer Rotation Vector (Sensor Fusion) for accuracy
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    
    private val _azimuth = MutableStateFlow(0f)
    val azimuth = _azimuth.asStateFlow()

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    
    private var lastAzimuth = 0f
    private val alpha = 0.05f // Smoothing factor (Low pass)

    fun start() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Fallback
            if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            if (magnetometer != null) sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            
            updateAzimuth(Math.toDegrees(orientation[0].toDouble()).toFloat())
            return
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values

        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                updateAzimuth(Math.toDegrees(orientation[0].toDouble()).toFloat())
            }
        }
    }

    private fun updateAzimuth(rawAzimuth: Float) {
        val azim = (rawAzimuth + 360) % 360
        
        // Smoothing: Linear Interpolation (Lerp) to avoid jitter
        // Handle 359 -> 1 wrap-around for smoothing
        var diff = azim - lastAzimuth
        if (diff > 180) diff -= 360
        if (diff < -180) diff += 360
        
        val newAzimuth = lastAzimuth + diff * alpha
        
        // Only emit if changed meaningfully to reduce state churn
        if (abs(newAzimuth - lastAzimuth) > 0.5) { 
             _azimuth.value = (newAzimuth + 360) % 360
             lastAzimuth = newAzimuth
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
