package com.example.floatingflavors

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.floatingflavors.app.core.di.PaymentResultBus
import com.example.floatingflavors.app.core.navigation.AppNavHost
import com.example.floatingflavors.app.core.ui.theme.FloatingFlavorsTheme
import com.example.floatingflavors.app.feature.admin.presentation.tracking.AdminPermissionHandler
import org.osmdroid.config.Configuration as OsmConfiguration
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.floatingflavors.app.feature.admin.presentation.tracking.service.LocationUpdateService
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationUpdateService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 101
        var pendingOrderIdForTracking: String? = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Init Network with Context for AuthInterceptor
        com.example.floatingflavors.app.core.network.NetworkClient.init(this)

        // Initialize osmdroid
        OsmConfiguration.getInstance().load(
            this,
            getSharedPreferences("osm", MODE_PRIVATE)
        )
        
        // Handle Notification Intent
        intent?.extras?.let { extras ->
            val screen = extras.getString("screen")
            val refId = extras.getString("reference_id")
            if (screen != null) {
                com.example.floatingflavors.app.core.navigation.PendingNotification.screen = screen
                com.example.floatingflavors.app.core.navigation.PendingNotification.referenceId = refId
                android.util.Log.d("MAIN_ACTIVITY", "Pending Notification: $screen ID: $refId")
            }
        }
        
        // 🔥 REQUEST NOTIFICATION PERMISSION (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 102)
            }
        }

        setContent {
            FloatingFlavorsApp()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent

        intent.extras?.let { extras ->
            val screen = extras.getString("screen")
            val refId = extras.getString("reference_id")
            
            if (screen != null) {
                // emit event to bus (since UI is already running)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    com.example.floatingflavors.app.core.service.NotificationEventBus.emitEvent(
                        com.example.floatingflavors.app.core.service.NotificationEvent.Navigate(screen, refId)
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == 102) {
             if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                 Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show()
             } else {
                 Toast.makeText(this, "Notifications are required for updates", Toast.LENGTH_LONG).show()
             }
             // Don't return, let location logic below run potentially
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.all {
                it == android.content.pm.PackageManager.PERMISSION_GRANTED
            }

            if (!granted) {
                Toast.makeText(
                    this,
                    "Location permission denied",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val locationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(
                    this,
                    "Please enable GPS and click Accept again",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            pendingOrderIdForTracking?.let { orderId ->
                val intent =
                    Intent(this, DeliveryLocationUpdateService::class.java).apply {
                        putExtra("ORDER_ID", orderId.toInt())
                        action = "START_TRACKING"
                    }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }

                Toast.makeText(
                    this,
                    "GPS tracking started",
                    Toast.LENGTH_LONG
                ).show()

                pendingOrderIdForTracking = null
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val response = data?.getStringExtra("response") ?: ""
            if (response.contains("SUCCESS", true)) {
                PaymentResultBus.emit("SUCCESS")
            } else {
                PaymentResultBus.emit("FAILURE")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FloatingFlavorsApp() {
    FloatingFlavorsTheme {
        AppNavHost()
    }
}


// BEFORE ADMIN GPS LOCATION USE PANNA FILE

//package com.example.floatingflavors
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.runtime.Composable
//import androidx.navigation.compose.rememberNavController
//import com.example.floatingflavors.app.core.navigation.AppNavHost
//import com.example.floatingflavors.app.core.navigation.AppShell
//import com.example.floatingflavors.app.core.navigation.Screen
//import com.example.floatingflavors.app.core.ui.theme.FloatingFlavorsTheme
//import com.example.floatingflavors.app.core.SessionManager
//import com.example.floatingflavors.app.core.di.PaymentResultBus
//import com.example.floatingflavors.app.feature.admin.presentation.tracking.AdminPermissionHandler
//
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // Initialize UserSession with saved userId
////        UserSession.initialize(this)
//
////        Log.d("MAIN_ACTIVITY", "Initialized UserSession with userId=${UserSession.userId}")
//        setContent {
//            FloatingFlavorsApp()
//        }
//    }
//
//    override fun onActivityResult(
//        requestCode: Int,
//        resultCode: Int,
//        data: Intent?
//    ) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 1001) {
//            val response = data?.getStringExtra("response") ?: ""
//            if (response.contains("SUCCESS", true)) {
//                PaymentResultBus.emit("SUCCESS")
//            } else {
//                PaymentResultBus.emit("FAILURE")
//            }
//        }
//    }
//}
//
//// In your Activity (e.g., MainActivity.kt)
//override fun onRequestPermissionsResult(
//    requestCode: Int,
//    permissions: Array<out String>,
//    grantResults: IntArray
//) {
//    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//    AdminPermissionHandler.handlePermissionResult(
//        requestCode,
//        grantResults,
//        onGranted = {
//            // Permission granted, you can now start tracking
//            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
//
//            // You can also broadcast this to your composables if needed
//            // e.g., using a shared ViewModel or LocalBroadcastManager
//        },
//        onDenied = {
//            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
//        }
//    )
//}
//
//@Composable
//fun FloatingFlavorsApp() {
//    FloatingFlavorsTheme {
//        AppNavHost() // root flow: Splash -> Onboarding -> Login -> AdminRoot/UserRoot
//    }
//}





//@Composable
//fun FloatingFlavorsApp() {
//    FloatingFlavorsTheme {
//        val navController = rememberNavController()
//        AppNavHost(navController = navController,)
//    }
//}
