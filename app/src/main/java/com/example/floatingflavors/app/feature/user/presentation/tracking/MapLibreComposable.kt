//package com.example.floatingflavors.app.feature.user.presentation.tracking
//
//import android.os.Bundle
//import android.view.ViewGroup
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.DefaultLifecycleObserver
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import org.maplibre.android.camera.CameraPosition
//import org.maplibre.android.geometry.LatLng
//import org.maplibre.android.maps.MapView
//import org.maplibre.android.maps.Style
//
//@Composable
//fun MapLibreComposable(
//    latitude: Double,
//    longitude: Double,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    val mapView = remember {
//        MapView(context).apply {
//            onCreate(Bundle())
//        }
//    }
//
//    DisposableEffect(lifecycleOwner) {
//        val observer = object : DefaultLifecycleObserver {
//            override fun onStart(owner: LifecycleOwner) {
//                mapView.onStart()
//            }
//
//            override fun onResume(owner: LifecycleOwner) {
//                mapView.onResume()
//            }
//
//            override fun onPause(owner: LifecycleOwner) {
//                mapView.onPause()
//            }
//
//            override fun onStop(owner: LifecycleOwner) {
//                mapView.onStop()
//            }
//
//            override fun onDestroy(owner: LifecycleOwner) {
//                mapView.onDestroy()
//            }
//        }
//
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }
//
//    AndroidView(
//        modifier = modifier,
//        factory = {
//            mapView.apply {
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//
//                getMapAsync { map ->
//                    map.setStyle(Style.MAPLIBRE_STREETS)
//                    map.cameraPosition = CameraPosition.Builder()
//                        .target(LatLng(latitude, longitude))
//                        .zoom(15.0)
//                        .build()
//                }
//            }
//        },
//        update = {
//            it.getMapAsync { map ->
//                map.cameraPosition = CameraPosition.Builder()
//                    .target(LatLng(latitude, longitude))
//                    .zoom(15.0)
//                    .build()
//            }
//        }
//    )
//}
