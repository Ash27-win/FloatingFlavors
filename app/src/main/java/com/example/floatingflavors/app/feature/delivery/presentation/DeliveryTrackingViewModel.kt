package com.example.floatingflavors.app.feature.delivery.presentation.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.delivery.data.DeliveryTrackingRepository
import com.example.floatingflavors.app.feature.user.data.tracking.OsrmRouteService
import com.example.floatingflavors.app.feature.user.data.tracking.TurnInstruction
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationStream

class DeliveryTrackingViewModel : ViewModel() {

    private val repository =
        DeliveryTrackingRepository(NetworkClient.deliveryTrackingApi)

    /* ---------------- STATE ---------------- */

    private val _liveLocation = MutableStateFlow<GeoPoint?>(null)
    val liveLocation: StateFlow<GeoPoint?> = _liveLocation.asStateFlow()

    private val _destination = MutableStateFlow<GeoPoint?>(null)
    val destination: StateFlow<GeoPoint?> = _destination.asStateFlow()

    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints.asStateFlow()

    private val _etaMinutes = MutableStateFlow(0)
    val etaMinutes: StateFlow<Int> = _etaMinutes.asStateFlow()

    private val _turns = MutableStateFlow<List<TurnInstruction>>(emptyList())
    val turns: StateFlow<List<TurnInstruction>> = _turns.asStateFlow()

    private val _currentTurn = MutableStateFlow<TurnInstruction?>(null)
    val currentTurn: StateFlow<TurnInstruction?> = _currentTurn.asStateFlow()

    private var pollingJob: Job? = null

    /* ---------------- ENTRY ---------------- */

    /**
     * Called ONCE from UI
     */
    fun startTracking(orderId: Int) {
        viewModelScope.launch {
            try {
                // 1️⃣ Destination (USER ADDRESS FROM ORDER)
                val tracking =
                    NetworkClient.deliveryTrackingApi.getTrackingDetails(orderId)

                val addr = tracking.deliveryAddress

                val lat = addr?.latitude ?: 0.0
                val lng = addr?.longitude ?: 0.0

                // ❌ No valid destination → stop
                if (
                    addr == null ||
                    lat == 0.0 ||
                    lng == 0.0
                ) {
                    _destination.value = null
                    _routePoints.value = emptyList()
                    _etaMinutes.value = 0
                    return@launch
                }

                val destPoint = GeoPoint(lat, lng)
                _destination.value = destPoint

                // 2️⃣ Start LIVE GPS polling
                startLivePolling(orderId, destPoint)

            } catch (e: Exception) {
                // backend fail → keep UI empty
            }
        }
    }

    /* ---------------- LIVE GPS + ROUTE ---------------- */

    /* ---------------- LIVE GPS + ROUTE ---------------- */
    private var lastRouteFetchTime = 0L

    private fun startLivePolling(orderId: Int, destination: GeoPoint) {
        pollingJob?.cancel()

        // LISTEN TO THE LOCAL STREAM (Blue Arrow Source)
        // This ensures Route & ETA matches the visual Marker exactly.
        pollingJob = viewModelScope.launch {
            DeliveryLocationStream.liveLocation.collect { livePoint ->
                if (livePoint == null) return@collect
                
                _liveLocation.value = livePoint

                // Dynamic ETA & Route Update
                // We shouldn't hit OSRM every 1s (Rate limits), so we throttle Route refreshing
                // But we can update Distance/ETA purely based on math if we wanted.
                // For now, let's try 2s throttle for OSRM to satisfy "dynamic" feel.
                val now = System.currentTimeMillis()
                if (now - lastRouteFetchTime > 2000) { 
                    lastRouteFetchTime = now
                    
                    try {
                         // 3️⃣ ROAD ROUTE (LIVE → DEST)
                         withContext(Dispatchers.IO) {
                            val (points, eta, instructions) =
                                OsrmRouteService.fetchRouteWithTurns(
                                    start = livePoint,
                                    end = destination
                                )
                            
                            withContext(Dispatchers.Main) {
                                _routePoints.value = points
                                _etaMinutes.value = eta
                                _turns.value = instructions
                                _currentTurn.value = instructions.firstOrNull()
                            }
                        }
                    } catch (e: Exception) {
                        // ignore route fail
                    }
                }
            }
        }
    }

    /* ---------------- ARRIVAL ---------------- */

    fun isArrived(): Boolean {
        val live = _liveLocation.value ?: return false
        val dest = _destination.value ?: return false
        return live.distanceToAsDouble(dest) < 30 // meters
    }

    fun markArrived(orderId: Int) {
        viewModelScope.launch {
            try {
                NetworkClient.deliveryTrackingApi.markArrived(orderId)
            } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
