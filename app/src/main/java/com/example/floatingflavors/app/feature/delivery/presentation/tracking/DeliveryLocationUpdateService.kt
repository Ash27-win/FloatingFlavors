package com.example.floatingflavors.app.feature.delivery.presentation.tracking

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.core.data.local.AppDatabase
import com.example.floatingflavors.app.feature.delivery.data.local.BufferedLocationEntity
import com.example.floatingflavors.app.feature.delivery.domain.WebSocketHeartbeatEngine
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private const val CHANNEL_ID = "delivery_tracking"
private const val NOTIFICATION_ID = 101

class DeliveryLocationUpdateService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var wakeLock: PowerManager.WakeLock? = null

    private var lastLat: Double? = null
    private var lastLng: Double? = null

    private var deliveryPartnerId = 0
    private var orderId = -1
    private var isTracking = false
    private var isLowBatteryMode = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db by lazy { AppDatabase.getDatabase(this) }
    private var syncJob: Job? = null

    // ── Feature 19: WebSocket Heartbeat ───────────────────────────────────────
    @Volatile private var isWebSocketAlive = true

    private val heartbeatEngine = WebSocketHeartbeatEngine(
        onPingSend = {
            // Probe connectivity by attempting a lightweight API call.
            // If this throws, WebSocketHeartbeatEngine will declare the connection dead
            // and switch the app into Offline Mode (SQLite buffering takes over).
            // TODO: Replace with a real WebSocket webSocket.send("{\"type\":\"PING\"}")
            //       once you integrate OkHttp WebSocket or SockJS on the backend.
            try {
                NetworkClient.deliveryApi.updateLiveLocation(
                    orderId = orderId,
                    lat = lastLat ?: 0.0,
                    lng = lastLng ?: 0.0,
                    deliveryPartnerId = deliveryPartnerId
                )
            } catch (e: Exception) {
                throw e  // Re-throw so heartbeat engine detects the failure
            }
        },
        onConnectionDead = {
            isWebSocketAlive = false
            Log.w("DELIVERY_GPS", "💔 WebSocket dead — switching to SQLite buffer mode.")
            // Notify the UI stream so the ViewModel can transition to OfflineMode
            DeliveryLocationStream.updateConnectionState(isOnline = false)
        },
        onConnectionRestored = {
            isWebSocketAlive = true
            Log.i("DELIVERY_GPS", "✅ WebSocket restored — resuming live sync.")
            DeliveryLocationStream.updateConnectionState(isOnline = true)
        }
    )

    private val batteryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level * 100 / scale.toFloat()

            if (batteryPct < 15.0f && !isLowBatteryMode) {
                isLowBatteryMode = true
                Log.w("DELIVERY_GPS", "🔋 Low Battery Mode Activated. Throttling GPS.")
                // Throttle GPS to save battery
                locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000).build()
                if (isTracking) {
                    stopLocationUpdates()
                    startLocationUpdates()
                }
            } else if (batteryPct >= 15.0f && isLowBatteryMode) {
                isLowBatteryMode = false
                Log.w("DELIVERY_GPS", "🔋 Battery Recovered. Restoring High Accuracy GPS.")
                locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()
                if (isTracking) {
                    stopLocationUpdates()
                    startLocationUpdates()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("DELIVERY_GPS", "📍 Service CREATED")

        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000 // 2 seconds
        )
            .setMinUpdateIntervalMillis(1000)
            .setMinUpdateDistanceMeters(0f) // 🔥 Report ALL movements, even small ones
            .setWaitForAccurateLocation(false)
            .build()

        createNotificationChannel()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            "START_TRACKING" -> {
                orderId = intent.getIntExtra("ORDER_ID", -1)
                deliveryPartnerId = intent.getIntExtra("DELIVERY_PARTNER_ID", 0)

                if (orderId == -1) {
                    Log.e("DELIVERY_GPS", "❌ orderId missing")
                    stopSelf()
                    return START_NOT_STICKY
                }
                
                // Removed strict manual check to rely on system enforcement in startForeground
                // This restores "Old Logic" flow where we attempt to start if UI says we can.

                if (!isLocationEnabled()) {
                    showEnableGpsNotification()
                    stopSelf()
                    return START_NOT_STICKY
                }

                requestIgnoreBatteryOptimizations()

                isTracking = true
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                        } catch (e: Exception) {
                            // Fallback for older SDKs if compilation fails (unlikely)
                        }
                        
                        startForeground(
                            NOTIFICATION_ID, 
                            buildNotification(), 
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, buildNotification())
                    }
                } catch (e: Exception) {
                    Log.e("DELIVERY_GPS", "❌ Failed to start foreground service: ${e.message}")
                    stopSelf()
                    return START_NOT_STICKY
                }
                acquireWakeLock()
                startLocationUpdates()
                startOfflineSyncWorker()
                heartbeatEngine.start()   // Feature 19: begin heartbeat loop
            }

            "STOP_TRACKING" -> {
                heartbeatEngine.stop()
                stopLocationUpdates()
                syncJob?.cancel()
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Delivery Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚚 Delivery Tracking Active")
            .setContentText("Order #$orderId • Live GPS")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FloatingFlavors:DeliveryGPS"
        ).apply { acquire() }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                ).apply {
                    data = android.net.Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    private fun showEnableGpsNotification() {
        Log.d("DELIVERY_GPS", "Showing GPS Enable Notification")
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Required")
            .setContentText("Enable location to start delivery tracking")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .addAction(0, "Enable GPS", pendingIntent)
            .build()

        try {
            // Even this needs permission if service type is location!
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
             Log.e("DELIVERY_GPS", "❌ Failed to show GPS notification: ${e.message}")
             // If we can't show notification due to permission, just die.
             stopSelf()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!isTracking) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return

                // 🔇 Filter GPS noise
                if (lastLat != null && lastLng != null) {
                    if (
                        abs(loc.latitude - lastLat!!) < 0.00005 &&
                        abs(loc.longitude - lastLng!!) < 0.00005
                    ) return
                }

                lastLat = loc.latitude
                lastLng = loc.longitude

                Log.d(
                    "DELIVERY_GPS",
                    "📍 LAT=${loc.latitude}, LNG=${loc.longitude}"
                )

                scope.launch {
                    try {
                        // Priority 1: Instant UI Update (Global Stream)
                        DeliveryLocationStream.updateLocation(
                            loc.latitude,
                            loc.longitude,
                            loc.bearing // 🔥 Send bearing
                        )

                        // Priority 2: Backend Sync
                        NetworkClient.deliveryApi.updateLiveLocation(
                            orderId = orderId,
                            lat = loc.latitude,
                            lng = loc.longitude,
                            deliveryPartnerId = deliveryPartnerId
                        )
                    } catch (e: Exception) {
                        Log.e("DELIVERY_GPS", "❌ API error, buffering offline", e)
                        db.bufferedLocationDao().insertLocation(
                            BufferedLocationEntity(
                                orderId = orderId,
                                deliveryPartnerId = deliveryPartnerId,
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                bearing = loc.bearing,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }

        fusedClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun startOfflineSyncWorker() {
        syncJob = scope.launch {
            while (isActive) {
                delay(10000) // Check every 10s
                try {
                    val pending = db.bufferedLocationDao().getPendingLocations()
                    if (pending.isNotEmpty()) {
                        // Log.d("DELIVERY_GPS", "🔄 Syncing ${pending.size} offline locations")
                        // In an enterprise app, we'd batch upload these.
                        // For now we'll upload the latest and clear, to catch up the dispatcher.
                        val latest = pending.last()
                        NetworkClient.deliveryApi.updateLiveLocation(
                            orderId = latest.orderId,
                            lat = latest.latitude,
                            lng = latest.longitude,
                            deliveryPartnerId = latest.deliveryPartnerId
                        )
                        db.bufferedLocationDao().deleteLocations(pending.map { it.id })
                    }
                } catch (e: Exception) {
                    // Log.e("DELIVERY_GPS", "❌ Offline Sync Failed", e)
                }
            }
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        heartbeatEngine.stop()           // Feature 19: clean shutdown
        stopLocationUpdates()
        releaseWakeLock()
        unregisterReceiver(batteryReceiver)
        scope.cancel()
        isTracking = false
        Log.d("DELIVERY_GPS", "🛑 Service DESTROYED")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}






//package com.example.floatingflavors.app.feature.delivery.presentation.tracking
//
//import android.annotation.SuppressLint
//import android.app.*
//import android.content.Context
//import android.content.Intent
//import android.location.Location
//import android.location.LocationManager
//import android.os.Build
//import android.os.IBinder
//import android.os.PowerManager
//import android.provider.Settings
//import android.util.Log
//import androidx.annotation.RequiresApi
//import androidx.core.app.NotificationCompat
//import com.example.floatingflavors.app.core.network.NetworkClient
//import com.google.android.gms.location.*
//import kotlinx.coroutines.*
//import java.io.File
//import java.io.FileWriter
//import java.text.SimpleDateFormat
//import java.util.*
//import java.util.concurrent.TimeUnit
//
//private const val CHANNEL_ID = "delivery_tracking"
//private const val NOTIFICATION_ID = 101
//
//class DeliveryLocationUpdateService : Service() {
//
//    private lateinit var fusedClient: FusedLocationProviderClient
//    private lateinit var callback: LocationCallback
//    private lateinit var locationRequest: LocationRequest
//    private var wakeLock: PowerManager.WakeLock? = null
//
//    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//    private var orderId: Int = -1
//    private var isTracking = false
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d("ADMIN_GPS", "📍 Service CREATED")
//        saveLogToFile("📍 Service CREATED")
//        fusedClient = LocationServices.getFusedLocationProviderClient(this)
//        setupLocationRequest()
//    }
//
//    private fun setupLocationRequest() {
//        locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            TimeUnit.SECONDS.toMillis(10) // Update every 10 seconds
//        )
//            .setWaitForAccurateLocation(true)
//            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
//            .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(15))
//            .build()
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Delivery Tracking",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Live location tracking for delivery"
//                setShowBadge(false)
//                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//            }
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    private fun getNotification(): Notification {
//        createNotificationChannel()
//
//        return NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("🚚 Delivery Tracking Active")
//            .setContentText("Order #$orderId • Live GPS sharing")
//            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
//            .setOngoing(true)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setCategory(NotificationCompat.CATEGORY_SERVICE)
//            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .setOnlyAlertOnce(true)
//            .build()
//    }
//
//    private fun isLocationEnabled(): Boolean {
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d("ADMIN_GPS", "📍 Service STARTING with action: ${intent?.action}")
//        saveLogToFile("📍 Service STARTING with action: ${intent?.action}")
//
//        // ✅ CHECK IF LOCATION IS ENABLED
//        if (!isLocationEnabled()) {
//            Log.e("ADMIN_GPS", "❌ Location/GPS is disabled on device")
//            saveLogToFile("❌ Location/GPS is disabled on device")
//
//            // Create error notification
//            val errorNotification = NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("⚠️ GPS Required")
//                .setContentText("Turn on Location to start tracking")
//                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .addAction(
//                    android.R.drawable.ic_menu_mylocation,
//                    "Enable GPS",
//                    PendingIntent.getActivity(
//                        this,
//                        0,
//                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
//                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//                    )
//                )
//                .build()
//
//            startForeground(NOTIFICATION_ID, errorNotification)
//            return START_STICKY
//        }
//
//        // Create notification first (required for foreground service)
//        startForeground(NOTIFICATION_ID, getNotification())
//
//        // ✅ FIXED: Add wake lock acquisition
//        try {
//            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
//            wakeLock = powerManager.newWakeLock(
//                PowerManager.PARTIAL_WAKE_LOCK,
//                "FloatingFlavors:LocationService"
//            ).apply {
//                setReferenceCounted(false)
//                acquire(10 * 60 * 1000L) // 10 minutes
//            }
//            Log.d("ADMIN_GPS", "🔋 Wake lock acquired")
//            saveLogToFile("🔋 Wake lock acquired")
//        } catch (e: SecurityException) {
//            Log.e("ADMIN_GPS", "❌ Failed to acquire wake lock: ${e.message}")
//            saveLogToFile("❌ Failed to acquire wake lock: ${e.message}")
//        }
//
//        when (intent?.action) {
//            "START_TRACKING" -> {
//                orderId = intent.getIntExtra("ORDER_ID", -1)
//                if (orderId != -1) {
//                    Log.d("ADMIN_GPS", "✅ Starting tracking for order $orderId")
//                    saveLogToFile("✅ Starting tracking for order $orderId")
//                    startLocationUpdates()
//                    isTracking = true
//                } else {
//                    Log.e("ADMIN_GPS", "❌ No ORDER_ID provided")
//                    saveLogToFile("❌ No ORDER_ID provided")
//                    stopSelf()
//                }
//            }
//            "STOP_TRACKING" -> {
//                Log.d("ADMIN_GPS", "🛑 Stopping tracking")
//                saveLogToFile("🛑 Stopping tracking")
//                stopSelf()
//            }
//            else -> {
//                orderId = intent?.getIntExtra("ORDER_ID", -1) ?: -1
//                if (orderId != -1) {
//                    Log.d("ADMIN_GPS", "✅ Starting tracking for order $orderId (default)")
//                    saveLogToFile("✅ Starting tracking for order $orderId (default)")
//                    startLocationUpdates()
//                    isTracking = true
//                } else {
//                    stopSelf()
//                }
//            }
//        }
//        return START_STICKY
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startLocationUpdates() {
//        if (!isTracking) return
//
//        // ✅ Check GPS status before starting
//        if (!isLocationEnabled()) {
//            Log.e("ADMIN_GPS", "❌ Cannot start location updates: GPS is disabled")
//            saveLogToFile("❌ Cannot start location updates: GPS is disabled")
//            stopSelf()
//            return
//        }
//
//        callback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                if (result.locations.isNotEmpty()) {
//                    val location = result.lastLocation!!
//                    val logMessage = "📍 LAT: ${"%.6f".format(location.latitude)}, " +
//                            "LNG: ${"%.6f".format(location.longitude)}, " +
//                            "ACC: ${"%.1f".format(location.accuracy)}m"
//
//                    Log.d("ADMIN_GPS", logMessage)
//                    saveLogToFile(logMessage)
//
//                    sendLocationToServer(location)
//                } else {
//                    val noLocationMessage = "📍 No location received - GPS might be weak"
//                    Log.d("ADMIN_GPS", noLocationMessage)
//                    saveLogToFile(noLocationMessage)
//                }
//            }
//
//            override fun onLocationAvailability(availability: LocationAvailability) {
//                val availabilityMessage = "📍 GPS Available: ${availability.isLocationAvailable}"
//                Log.d("ADMIN_GPS", availabilityMessage)
//                saveLogToFile(availabilityMessage)
//
//                if (!availability.isLocationAvailable) {
//                    val weakSignalMessage = "⚠️ GPS signal lost or weak"
//                    Log.w("ADMIN_GPS", weakSignalMessage)
//                    saveLogToFile(weakSignalMessage)
//                }
//            }
//        }
//
//        try {
//            fusedClient.requestLocationUpdates(
//                locationRequest,
//                callback,
//                mainLooper
//            )
//            Log.d("ADMIN_GPS", "✅ Location updates started for order $orderId")
//            saveLogToFile("✅ Location updates started for order $orderId")
//
//            // Log initial GPS status
//            val gpsStatusMessage = "📡 GPS Status: ${if (isLocationEnabled()) "ENABLED" else "DISABLED"}"
//            Log.d("ADMIN_GPS", gpsStatusMessage)
//            saveLogToFile(gpsStatusMessage)
//
//            // Try to get last known location immediately
//            try {
//                fusedClient.lastLocation.addOnSuccessListener { lastLocation ->
//                    if (lastLocation != null) {
//                        val lastLocMessage = "📍 Last known location: " +
//                                "${"%.6f".format(lastLocation.latitude)}, " +
//                                "${"%.6f".format(lastLocation.longitude)}"
//                        Log.d("ADMIN_GPS", lastLocMessage)
//                        saveLogToFile(lastLocMessage)
//                        sendLocationToServer(lastLocation)
//                    } else {
//                        val noLastLocMessage = "📍 No last known location available"
//                        Log.d("ADMIN_GPS", noLastLocMessage)
//                        saveLogToFile(noLastLocMessage)
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("ADMIN_GPS", "❌ Error getting last location: ${e.message}")
//                saveLogToFile("❌ Error getting last location: ${e.message}")
//            }
//
//        } catch (e: SecurityException) {
//            Log.e("ADMIN_GPS", "❌ SecurityException: ${e.message}")
//            saveLogToFile("❌ SecurityException: ${e.message}")
//            stopSelf()
//        } catch (e: Exception) {
//            Log.e("ADMIN_GPS", "❌ Failed to start location updates: ${e.message}")
//            saveLogToFile("❌ Failed to start location updates: ${e.message}")
//            stopSelf()
//        }
//    }
//
//    private fun sendLocationToServer(location: Location) {
//        if (orderId == -1) return
//
//        val locationMessage = "📡 Sending location to server: " +
//                "lat=${"%.6f".format(location.latitude)}, " +
//                "lng=${"%.6f".format(location.longitude)}"
//
//        Log.d("ADMIN_GPS", locationMessage)
//        saveLogToFile(locationMessage)
//
//        scope.launch {
//            try {
//                // Try adminLocationApi first
//                // In sendLocationToServer() method, change:
//                NetworkClient.deliveryLocationApi.updateLocation(  // ✅ Use delivery API
//                    orderId = orderId,
//                    lat = location.latitude,
//                    lng = location.longitude
//                )
//                val successMessage = "✅ Location sent to server for order $orderId"
//                Log.d("ADMIN_GPS", successMessage)
//                saveLogToFile(successMessage)
//
//            } catch (e: Exception) {
//                val errorMessage = "❌ Failed to send location via admin API: ${e.message}"
//                Log.e("ADMIN_GPS", errorMessage)
//                saveLogToFile(errorMessage)
//                e.printStackTrace()
//
//                // Try alternative API
//                try {
//                    NetworkClient.orderTrackingApi.updateOrderLocation(
//                        orderId = orderId,
//                        lat = location.latitude,
//                        lng = location.longitude
//                    )
//                    val altSuccessMessage = "✅ Location sent via orderTrackingApi"
//                    Log.d("ADMIN_GPS", altSuccessMessage)
//                    saveLogToFile(altSuccessMessage)
//                } catch (e2: Exception) {
//                    val altErrorMessage = "❌ Failed all API attempts: ${e2.message}"
//                    Log.e("ADMIN_GPS", altErrorMessage)
//                    saveLogToFile(altErrorMessage)
//                    e2.printStackTrace()
//                }
//            }
//        }
//    }
//
//    private fun saveLogToFile(message: String) {
//        try {
//            val logFile = File(getExternalFilesDir(null), "gps_log.txt")
//            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
//            val logEntry = "[$timestamp] $message\n"
//
//            FileWriter(logFile, true).use { writer ->
//                writer.append(logEntry)
//            }
//        } catch (e: Exception) {
//            // Ignore file errors
//            Log.e("ADMIN_GPS", "❌ Failed to save log to file: ${e.message}")
//        }
//    }
//
//    private fun stopLocationUpdates() {
//        try {
//            if (::callback.isInitialized) {
//                fusedClient.removeLocationUpdates(callback)
//                Log.d("ADMIN_GPS", "📍 Location updates stopped")
//                saveLogToFile("📍 Location updates stopped")
//            }
//        } catch (e: Exception) {
//            Log.e("ADMIN_GPS", "Error stopping location updates: ${e.message}")
//            saveLogToFile("Error stopping location updates: ${e.message}")
//        }
//    }
//
//    override fun onDestroy() {
//        stopLocationUpdates()
//        wakeLock?.let {
//            try {
//                if (it.isHeld) {
//                    it.release()
//                    Log.d("ADMIN_GPS", "🔋 Wake lock released")
//                    saveLogToFile("🔋 Wake lock released")
//                }
//            } catch (e: Exception) {
//                Log.e("ADMIN_GPS", "Error releasing wake lock: ${e.message}")
//                saveLogToFile("Error releasing wake lock: ${e.message}")
//            }
//        }
//        scope.cancel()
//        isTracking = false
//        Log.d("ADMIN_GPS", "🛑 Service destroyed")
//        saveLogToFile("🛑 Service destroyed")
//        super.onDestroy()
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//}