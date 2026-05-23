package com.example.floatingflavors.app.feature.delivery.presentation.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.core.data.local.AppDatabase
import com.example.floatingflavors.app.feature.delivery.data.DeliveryTrackingRepository
import com.example.floatingflavors.app.feature.user.presentation.tracking.NavigationRoute
import com.example.floatingflavors.app.feature.delivery.domain.OrsDecoder
import com.example.floatingflavors.app.feature.delivery.data.OpenRouteServiceApi
import com.example.floatingflavors.app.feature.delivery.data.local.RouteCacheEntity
import com.example.floatingflavors.app.feature.delivery.domain.ActiveSessionRepository
import com.example.floatingflavors.app.feature.delivery.domain.RerouteCooldownEngine
import com.example.floatingflavors.app.feature.delivery.domain.TrafficConfidenceEngine
import com.example.floatingflavors.app.feature.user.data.tracking.OsrmRouteService
import com.example.floatingflavors.app.feature.user.presentation.tracking.RouteSegment
import java.util.UUID
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationStream
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

sealed class NavigationPhase {
    object Idle : NavigationPhase()
    object NavigatingToPickup : NavigationPhase()
    object WaitingAtStore : NavigationPhase()
    object DeliveringOrder : NavigationPhase()
    object ReturningToHub : NavigationPhase()
    object ReRouting : NavigationPhase()
    object OfflineMode : NavigationPhase()
}

class DeliveryTrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DeliveryTrackingRepository(NetworkClient.deliveryTrackingApi)
    
    // Independent ORS Client
    private val orsApi = Retrofit.Builder()
        .baseUrl("https://api.openrouteservice.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenRouteServiceApi::class.java)

    // Enterprise Navigation Engines
//    private val kalmanFilter = com.example.floatingflavors.app.feature.delivery.domain.KalmanLocationFilter()
    private val sessionRepo = ActiveSessionRepository(application)
    private val rerouteCooldown = RerouteCooldownEngine()   // Feature 4
    // Feature 15: Traffic Confidence — reset on each new order
    private val trafficConfidence = TrafficConfidenceEngine
    private val db = AppDatabase.getDatabase(application)
    private val gson = Gson()

    private val deliveryRepo = com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository(NetworkClient.deliveryApi)

    // Traffic warning banner for the UI (Feature 15)
    private val _trafficWarningBanner = MutableStateFlow<String?>(null)
    val trafficWarningBanner: StateFlow<String?> = _trafficWarningBanner.asStateFlow()

    // Order Details for Trip Info
    private val _orderDetails = MutableStateFlow<com.example.floatingflavors.app.feature.delivery.data.DeliveryOrderDetailsResponse?>(null)
    val orderDetails: StateFlow<com.example.floatingflavors.app.feature.delivery.data.DeliveryOrderDetailsResponse?> = _orderDetails.asStateFlow()

    /* ---------------- STATE ---------------- */

    private val _navigationPhase = MutableStateFlow<NavigationPhase>(NavigationPhase.DeliveringOrder)
    val navigationPhase: StateFlow<NavigationPhase> = _navigationPhase.asStateFlow()

    private val _liveLocation = MutableStateFlow<GeoPoint?>(null)
    val liveLocation: StateFlow<GeoPoint?> = _liveLocation.asStateFlow()

    private val _destination = MutableStateFlow<GeoPoint?>(null)
    val destination: StateFlow<GeoPoint?> = _destination.asStateFlow()

    private val _routes = MutableStateFlow<List<NavigationRoute>>(emptyList())
    val routes: StateFlow<List<NavigationRoute>> = _routes.asStateFlow()

    private val _activeRouteId = MutableStateFlow<String?>(null)
    val activeRouteId: StateFlow<String?> = _activeRouteId.asStateFlow()

    private val _etaMinutes = MutableStateFlow(0)
    val etaMinutes: StateFlow<Int> = _etaMinutes.asStateFlow()

    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm.asStateFlow()

    // Balanced Features
    private val _speedKmh = MutableStateFlow(0.0)
    val speedKmh: StateFlow<Double> = _speedKmh.asStateFlow()

    private val _gpsAccuracyMeters = MutableStateFlow(4.5) 
    val gpsAccuracyMeters: StateFlow<Double> = _gpsAccuracyMeters.asStateFlow()

    private val _isDeviating = MutableStateFlow(false)
    val isDeviating: StateFlow<Boolean> = _isDeviating.asStateFlow()

    // Enterprise Navigation Engines
    private val kalmanFilter = com.example.floatingflavors.app.feature.delivery.domain.KalmanLocationFilter()
    
    // Historical Speed for Smart ETA (last 60 ticks)
    private val speedHistory = ArrayDeque<Double>()

    private var pollingJob: Job? = null
    private var onlineObserverJob: Job? = null
    private var lastLocation: GeoPoint? = null
    private var lastTimeMs: Long = 0

    /* ---------------- ENTRY ---------------- */

    fun startTracking(orderId: Int) {
        viewModelScope.launch {
            try {
                // Fetch full order details for Trip Details UI
                val details = deliveryRepo.getOrderDetails(orderId)
                _orderDetails.value = details
                
                val tracking = NetworkClient.deliveryTrackingApi.getTrackingDetails(orderId)
                val addr = tracking.deliveryAddress
                val lat = addr?.latitude ?: 0.0
                val lng = addr?.longitude ?: 0.0

                if (addr == null || lat == 0.0 || lng == 0.0) return@launch

                val destPoint = GeoPoint(lat, lng)
                _destination.value = destPoint

                startLivePolling(orderId, destPoint)
                observeConnectionHealth()
            } catch (e: Exception) {}
        }
    }

    /* ---------------- LIVE GPS + ROUTE ---------------- */

    private var lastRouteFetchTime = 0L

    private fun startLivePolling(orderId: Int, initialDestination: GeoPoint) {
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {
            DeliveryLocationStream.liveLocation.collect { rawLivePoint ->
                if (rawLivePoint == null) return@collect
                
                // 1. Simulate Raw GPS Accuracy (Normally comes from Android Location object)
                val rawAccuracy = 4.0 + (Math.random() * 6.0)
                _gpsAccuracyMeters.value = rawAccuracy
                
                // 2. Apply Kalman Smoothing
                val livePoint = kalmanFilter.processLocation(
                    lat = rawLivePoint.latitude,
                    lng = rawLivePoint.longitude,
                    accuracy = rawAccuracy
                )
                
                // 3. Calculate Speed using Smoothed Points
                val now = System.currentTimeMillis()
                if (lastLocation != null && lastTimeMs > 0) {
                    val dist = lastLocation!!.distanceToAsDouble(livePoint)
                    val timeSec = (now - lastTimeMs) / 1000.0
                    if (timeSec > 0) {
                        val speedMs = dist / timeSec
                        val kmh = speedMs * 3.6
                        _speedKmh.value = kmh
                        
                        // Smart ETA: 60-second rolling average
                        speedHistory.addLast(kmh)
                        if (speedHistory.size > 60) speedHistory.removeFirst()
                        
                        // Dynamically decrease distance
                        _distanceKm.value = (_distanceKm.value - (dist / 1000.0)).coerceAtLeast(0.0)

                        // Recalculate ETA dynamically if we have a route
                        if (_distanceKm.value > 0.0) {
                            val avgKmh = speedHistory.average().coerceAtLeast(10.0) // Assume min 10kmh for traffic
                            val newEta = (_distanceKm.value / avgKmh) * 60.0
                            _etaMinutes.value = newEta.toInt()
                        }
                    }
                }
                lastLocation = livePoint
                lastTimeMs = now
                
                // 4. Snap to Road
                val activeRoute = _routes.value.find { it.id == _activeRouteId.value }
                val finalRenderPoint = if (_isDeviating.value) {
                    livePoint // Free-roam if off-route
                } else {
                    com.example.floatingflavors.app.feature.delivery.domain.TurfSnapEngine.snapToRoute(livePoint, activeRoute)
                }
                
                _liveLocation.value = finalRenderPoint

                // 5. Smart Reroute with Cooldown Guard
                val routePoints = activeRoute?.segments?.flatMap { it.points } ?: emptyList()
                val shouldReroute = rerouteCooldown.shouldReroute(livePoint, routePoints)

                if (shouldReroute) {
                    _isDeviating.value = true
                    lastRouteFetchTime = 0L  // Force immediate refetch
                }

                // 6. Smart ORS Route Fetching 
                // Only fetch if deviating, OR if it's been 30 seconds (optimized from 15s)
                val shouldFetchRoute = (now - lastRouteFetchTime > 30000) || _isDeviating.value
                
                if (shouldFetchRoute) { 
                    lastRouteFetchTime = now
                    fetchOrsRoute(livePoint)
                }
            }
        }
    }

    private fun fetchOrsRoute(currentLocation: GeoPoint) {
        viewModelScope.launch {
            try {
                val activeDestination = if (_navigationPhase.value is NavigationPhase.ReturningToHub) {
                    GeoPoint(12.551690, 80.163351) // Floating Flavors Hub
                } else {
                    _destination.value
                }

                if (activeDestination == null) return@launch

                val osrmRoutes = OsrmRouteService.fetchMultipleRoutes(currentLocation, activeDestination)

                if (osrmRoutes.isNotEmpty()) {
                    // Feature 15: mark traffic data as fresh
                    trafficConfidence.onTrafficDataReceived()
                    _trafficWarningBanner.value = null

                    val decodedRoutes = osrmRoutes.mapIndexed { index, routeResult ->
                        val segments = mutableListOf<RouteSegment>()
                        val chunkedPoints = routeResult.points.chunked(routeResult.points.size / 3 + 1)
                        val colors = listOf(
                            android.graphics.Color.parseColor("#34A853"), // Green
                            android.graphics.Color.parseColor("#FBBC05"), // Amber
                            android.graphics.Color.parseColor("#EA4335")  // Red
                        )
                        chunkedPoints.forEachIndexed { chunkIndex, chunk ->
                            if (chunk.isNotEmpty()) {
                                segments.add(RouteSegment(chunk, colors[chunkIndex % colors.size]))
                            }
                        }

                        NavigationRoute(
                            id = "route_${UUID.randomUUID()}",
                            segments = segments,
                            totalDistanceKm = routeResult.distanceMeters / 1000.0,
                            totalEtaMinutes = routeResult.durationMin,
                            isFastest = index == 0
                        )
                    }
                             
                    withContext(Dispatchers.Main) {
                        _routes.value = decodedRoutes
                        if (_activeRouteId.value == null || _isDeviating.value) {
                            val fastest = decodedRoutes.firstOrNull()
                            _activeRouteId.value = fastest?.id
                            _etaMinutes.value = fastest?.totalEtaMinutes ?: 0
                            _distanceKm.value = fastest?.totalDistanceKm ?: 0.0
                            _isDeviating.value = false
                        } else {
                            val active = decodedRoutes.find { it.id == _activeRouteId.value }
                            _etaMinutes.value = active?.totalEtaMinutes ?: 0
                            _distanceKm.value = active?.totalDistanceKm ?: 0.0
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore route fail
            }
        }
    }
    
    private fun observeConnectionHealth() {
        onlineObserverJob?.cancel()
        onlineObserverJob = viewModelScope.launch {
            DeliveryLocationStream.isOnline.collect { isOnline ->
                if (!isOnline && _navigationPhase.value !is NavigationPhase.OfflineMode) {
                    _navigationPhase.value = NavigationPhase.OfflineMode
                } else if (isOnline && _navigationPhase.value is NavigationPhase.OfflineMode) {
                    // Restore to delivery phase when connection comes back
                    _navigationPhase.value = NavigationPhase.DeliveringOrder
                }
            }
        }
    }

    fun selectRoute(routeId: String) {
        _activeRouteId.value = routeId
        val route = _routes.value.find { it.id == routeId }
        _etaMinutes.value = route?.totalEtaMinutes ?: 0
        _distanceKm.value = route?.totalDistanceKm ?: 0.0
    }

    fun isArrived(): Boolean {
        val live = _liveLocation.value ?: return false
        val dest = _destination.value ?: return false
        return live.distanceToAsDouble(dest) < 50 // meters
    }

    fun markArrived(orderId: Int) {
        viewModelScope.launch {
            try { NetworkClient.deliveryTrackingApi.markArrived(orderId) } catch (_: Exception) {}
            
            _navigationPhase.value = NavigationPhase.ReturningToHub
            _destination.value?.let { startLivePolling(orderId, it) }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        onlineObserverJob?.cancel()
        super.onCleared()
    }
}
