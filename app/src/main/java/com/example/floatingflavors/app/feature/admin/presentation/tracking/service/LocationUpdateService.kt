package com.example.floatingflavors.app.feature.admin.presentation.tracking.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.floatingflavors.app.core.network.NetworkClient
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "delivery_tracking"
private const val NOTIFICATION_ID = 101

class LocationUpdateService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var callback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var wakeLock: PowerManager.WakeLock? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var orderId: Int = -1
    private var isTracking = false

    override fun onCreate() {
        super.onCreate()
        Log.d("ADMIN_GPS", "📍 Service CREATED")
        saveLogToFile("📍 Service CREATED")
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationRequest()
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(10) // Update every 10 seconds
        )
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(15))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Delivery Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live location tracking for delivery"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚚 Delivery Tracking Active")
            .setContentText("Order #$orderId • Live GPS sharing")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ADMIN_GPS", "📍 Service STARTING with action: ${intent?.action}")
        saveLogToFile("📍 Service STARTING with action: ${intent?.action}")

        // ✅ CHECK IF LOCATION IS ENABLED
        if (!isLocationEnabled()) {
            Log.e("ADMIN_GPS", "❌ Location/GPS is disabled on device")
            saveLogToFile("❌ Location/GPS is disabled on device")

            // Create error notification
            val errorNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("⚠️ GPS Required")
                .setContentText("Turn on Location to start tracking")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(
                    android.R.drawable.ic_menu_mylocation,
                    "Enable GPS",
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()

            startForeground(NOTIFICATION_ID, errorNotification)
            return START_STICKY
        }

        // Create notification first (required for foreground service)
        startForeground(NOTIFICATION_ID, getNotification())

        // ✅ FIXED: Add wake lock acquisition
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "FloatingFlavors:LocationService"
            ).apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes
            }
            Log.d("ADMIN_GPS", "🔋 Wake lock acquired")
            saveLogToFile("🔋 Wake lock acquired")
        } catch (e: SecurityException) {
            Log.e("ADMIN_GPS", "❌ Failed to acquire wake lock: ${e.message}")
            saveLogToFile("❌ Failed to acquire wake lock: ${e.message}")
        }

        when (intent?.action) {
            "START_TRACKING" -> {
                orderId = intent.getIntExtra("ORDER_ID", -1)
                if (orderId != -1) {
                    Log.d("ADMIN_GPS", "✅ Starting tracking for order $orderId")
                    saveLogToFile("✅ Starting tracking for order $orderId")
                    startLocationUpdates()
                    isTracking = true
                } else {
                    Log.e("ADMIN_GPS", "❌ No ORDER_ID provided")
                    saveLogToFile("❌ No ORDER_ID provided")
                    stopSelf()
                }
            }
            "STOP_TRACKING" -> {
                Log.d("ADMIN_GPS", "🛑 Stopping tracking")
                saveLogToFile("🛑 Stopping tracking")
                stopSelf()
            }
            else -> {
                orderId = intent?.getIntExtra("ORDER_ID", -1) ?: -1
                if (orderId != -1) {
                    Log.d("ADMIN_GPS", "✅ Starting tracking for order $orderId (default)")
                    saveLogToFile("✅ Starting tracking for order $orderId (default)")
                    startLocationUpdates()
                    isTracking = true
                } else {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!isTracking) return

        // ✅ Check GPS status before starting
        if (!isLocationEnabled()) {
            Log.e("ADMIN_GPS", "❌ Cannot start location updates: GPS is disabled")
            saveLogToFile("❌ Cannot start location updates: GPS is disabled")
            stopSelf()
            return
        }

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (result.locations.isNotEmpty()) {
                    val location = result.lastLocation!!
                    val logMessage = "📍 LAT: ${"%.6f".format(location.latitude)}, " +
                            "LNG: ${"%.6f".format(location.longitude)}, " +
                            "ACC: ${"%.1f".format(location.accuracy)}m"

                    Log.d("ADMIN_GPS", logMessage)
                    saveLogToFile(logMessage)

                    sendLocationToServer(location)
                } else {
                    val noLocationMessage = "📍 No location received - GPS might be weak"
                    Log.d("ADMIN_GPS", noLocationMessage)
                    saveLogToFile(noLocationMessage)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                val availabilityMessage = "📍 GPS Available: ${availability.isLocationAvailable}"
                Log.d("ADMIN_GPS", availabilityMessage)
                saveLogToFile(availabilityMessage)

                if (!availability.isLocationAvailable) {
                    val weakSignalMessage = "⚠️ GPS signal lost or weak"
                    Log.w("ADMIN_GPS", weakSignalMessage)
                    saveLogToFile(weakSignalMessage)
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                callback,
                mainLooper
            )
            Log.d("ADMIN_GPS", "✅ Location updates started for order $orderId")
            saveLogToFile("✅ Location updates started for order $orderId")

            // Log initial GPS status
            val gpsStatusMessage = "📡 GPS Status: ${if (isLocationEnabled()) "ENABLED" else "DISABLED"}"
            Log.d("ADMIN_GPS", gpsStatusMessage)
            saveLogToFile(gpsStatusMessage)

            // Try to get last known location immediately
            try {
                fusedClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) {
                        val lastLocMessage = "📍 Last known location: " +
                                "${"%.6f".format(lastLocation.latitude)}, " +
                                "${"%.6f".format(lastLocation.longitude)}"
                        Log.d("ADMIN_GPS", lastLocMessage)
                        saveLogToFile(lastLocMessage)
                        sendLocationToServer(lastLocation)
                    } else {
                        val noLastLocMessage = "📍 No last known location available"
                        Log.d("ADMIN_GPS", noLastLocMessage)
                        saveLogToFile(noLastLocMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e("ADMIN_GPS", "❌ Error getting last location: ${e.message}")
                saveLogToFile("❌ Error getting last location: ${e.message}")
            }

        } catch (e: SecurityException) {
            Log.e("ADMIN_GPS", "❌ SecurityException: ${e.message}")
            saveLogToFile("❌ SecurityException: ${e.message}")
            stopSelf()
        } catch (e: Exception) {
            Log.e("ADMIN_GPS", "❌ Failed to start location updates: ${e.message}")
            saveLogToFile("❌ Failed to start location updates: ${e.message}")
            stopSelf()
        }
    }

    private fun sendLocationToServer(location: Location) {
        if (orderId == -1) return

        val locationMessage = "📡 Sending location to server: " +
                "lat=${"%.6f".format(location.latitude)}, " +
                "lng=${"%.6f".format(location.longitude)}"

        Log.d("ADMIN_GPS", locationMessage)
        saveLogToFile(locationMessage)

        scope.launch {
            try {
                // Try adminLocationApi first
                NetworkClient.adminLocationApi.updateLocation(
                    orderId = orderId,
                    lat = location.latitude,
                    lng = location.longitude
                )
                val successMessage = "✅ Location sent to server for order $orderId"
                Log.d("ADMIN_GPS", successMessage)
                saveLogToFile(successMessage)

            } catch (e: Exception) {
                val errorMessage = "❌ Failed to send location via admin API: ${e.message}"
                Log.e("ADMIN_GPS", errorMessage)
                saveLogToFile(errorMessage)
                e.printStackTrace()

                // Try alternative API
                try {
                    NetworkClient.orderTrackingApi.updateLiveLocation(
                        orderId = orderId,
                        lat = location.latitude,
                        lng = location.longitude,
                        deliveryPartnerId = 1 // Admin Test
                    )
                    val altSuccessMessage = "✅ Location sent via orderTrackingApi"
                    Log.d("ADMIN_GPS", altSuccessMessage)
                    saveLogToFile(altSuccessMessage)
                } catch (e2: Exception) {
                    val altErrorMessage = "❌ Failed all API attempts: ${e2.message}"
                    Log.e("ADMIN_GPS", altErrorMessage)
                    saveLogToFile(altErrorMessage)
                    e2.printStackTrace()
                }
            }
        }
    }

    private fun saveLogToFile(message: String) {
        try {
            val logFile = File(getExternalFilesDir(null), "gps_log.txt")
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] $message\n"

            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
        } catch (e: Exception) {
            // Ignore file errors
            Log.e("ADMIN_GPS", "❌ Failed to save log to file: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        try {
            if (::callback.isInitialized) {
                fusedClient.removeLocationUpdates(callback)
                Log.d("ADMIN_GPS", "📍 Location updates stopped")
                saveLogToFile("📍 Location updates stopped")
            }
        } catch (e: Exception) {
            Log.e("ADMIN_GPS", "Error stopping location updates: ${e.message}")
            saveLogToFile("Error stopping location updates: ${e.message}")
        }
    }

    override fun onDestroy() {
        stopLocationUpdates()
        wakeLock?.let {
            try {
                if (it.isHeld) {
                    it.release()
                    Log.d("ADMIN_GPS", "🔋 Wake lock released")
                    saveLogToFile("🔋 Wake lock released")
                }
            } catch (e: Exception) {
                Log.e("ADMIN_GPS", "Error releasing wake lock: ${e.message}")
                saveLogToFile("Error releasing wake lock: ${e.message}")
            }
        }
        scope.cancel()
        isTracking = false
        Log.d("ADMIN_GPS", "🛑 Service destroyed")
        saveLogToFile("🛑 Service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}