package com.example.floatingflavors.app.feature.delivery.presentation.tracking

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.floatingflavors.MainActivity

object DeliveryPermissionHandler {

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun isGpsEnabled(context: Context): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun openGpsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
}





//package com.example.floatingflavors.app.feature.delivery.presentation.tracking
//
//import android.Manifest
//import android.app.Activity
//import android.content.pm.PackageManager
//import android.os.Build
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//
//object DeliveryPermissionHandler {
//
//    fun hasLocationPermission(activity: Activity): Boolean {
//        val fineLocationGranted = ContextCompat.checkSelfPermission(
//            activity,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//
//        val coarseLocationGranted = ContextCompat.checkSelfPermission(
//            activity,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//
//        // For Android 10+, we also need background location permission
//        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ContextCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        } else {
//            true // Background location not required before Android 10
//        }
//
//        return fineLocationGranted && coarseLocationGranted && backgroundLocationGranted
//    }
//
//    fun requestLocationPermission(activity: Activity) {
//        val permissions = mutableListOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//        }
//
//        ActivityCompat.requestPermissions(
//            activity,
//            permissions.toTypedArray(),
//            DELIVERY_LOCATION_PERMISSION_REQUEST_CODE
//        )
//    }
//
//    fun handlePermissionResult(
//        requestCode: Int,
//        grantResults: IntArray,
//        onGranted: () -> Unit,
//        onDenied: () -> Unit
//    ) {
//        if (requestCode == DELIVERY_LOCATION_PERMISSION_REQUEST_CODE) {
//            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
//            if (allGranted) {
//                onGranted()
//            } else {
//                onDenied()
//            }
//        }
//    }
//
//    const val DELIVERY_LOCATION_PERMISSION_REQUEST_CODE = 201
//}