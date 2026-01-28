//package com.example.floatingflavors.app.feature.delivery.presentation.tracking
//
//import androidx.compose.runtime.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun DeliveryTrackingMapScreen(
//    orderId: Int,
//    viewModel: DeliveryTrackingViewModel
//) {
//    val route by viewModel.selectedRoute.collectAsState()
//    val eta by viewModel.etaMinutes.collectAsState()
//    val distance by viewModel.distanceKm.collectAsState()
//    val nextTurn by viewModel.nextTurn.collectAsState()
//
//    Box(Modifier.fillMaxSize()) {
//
//        OsmMapComposable(
//            routeOptions = viewModel.routes.collectAsState().value,
//            selectedRoute = route,
//            onRouteSelected = viewModel::selectRoute,
//            deliveryLocation = viewModel.deliveryLocation.collectAsState().value,
//            destination = viewModel.destination
//        )
//
//        nextTurn?.let {
//            NextTurnCard(
//                title = it.primary,
//                distance = it.distance
//            )
//        }
//
//        DeliveryBottomSheet(
//            etaMin = eta,
//            distanceKm = distance,
//            onArrived = { viewModel.markArrived(orderId) }
//        )
//    }
//}
