package com.example.floatingflavors.app.feature.delivery.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.OrderItemDto
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationUpdateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.location.Geocoder
import kotlinx.coroutines.isActive
import java.util.Locale

/* -------------------- UI MODEL -------------------- */

data class DeliveryOrder(
    val id: String,
    val customerName: String,
    val customerPhone: String? = null,
    val status: String,
    val amount: String,
    val deliveryPartnerId: String?,
    val items: List<OrderItemDto>,
    val distance: String?,
    val createdAt: String?,
    val deliveryAddress: String?
)

/* -------------------- VIEWMODEL -------------------- */

class DeliveryOrderDetailsViewModel(
    private val orderId: Int,
    private val deliveryPartnerId: Int
) : ViewModel() {

    private val repository = DeliveryRepository()

    private val _order = MutableStateFlow<DeliveryOrder?>(null)
    val order: StateFlow<DeliveryOrder?> = _order

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _pickupAddress = MutableStateFlow("Fetching live location...")
    val pickupAddress: StateFlow<String> = _pickupAddress

    private val _acceptSuccess = MutableStateFlow(false)
    val acceptSuccess: StateFlow<Boolean> = _acceptSuccess

    // 🔒 OBSERVER CONTROL
    private var observeJob: kotlinx.coroutines.Job? = null

    /* ---------------- ORDER DETAILS ---------------- */

    fun loadOrderDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.getOrderDetails(orderId)
                if (res.success && res.data != null) {
                    val d = res.data
                    _order.value = DeliveryOrder(
                        id = d.id!!,
                        customerName = d.customerName!!,
                        customerPhone = d.customerPhone,
                        status = d.status!!,
                        amount = d.amount!!,
                        deliveryPartnerId = d.deliveryPartnerId,
                        items = d.items ?: emptyList(),
                        distance = d.distance,
                        createdAt = d.createdAt,
                        deliveryAddress = d.deliveryAddress
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* ---------------- LIVE PICKUP OBSERVER ---------------- */

    fun startObservingPickup(context: Context) {
        if (observeJob != null) return

        observeJob = viewModelScope.launch {
            while (isActive) {
                // If order is active (OUT_FOR_DELIVERY), fetch from backend (Tracking)
                // If not, we might want to show local device location if available?
                val loc = repository.getLastLiveLocation(orderId)
                if (loc != null) {
                    updatePickupAddressFromLocation(
                        context,
                        loc.latitude,
                        loc.longitude
                    )
                }
                kotlinx.coroutines.delay(8000)
            }
        }
    }

    // ✅ NEW: Explicitly fetch device location (for preview before accept)
    fun fetchDeviceLocation(context: Context) {
        try {
             val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
             
             // Check permission
             if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == 
                 android.content.pm.PackageManager.PERMISSION_GRANTED) {
                 
                 val lastKnown = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER) 
                     ?: lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                 
                 if (lastKnown != null) {
                     updatePickupAddressFromLocation(context, lastKnown.latitude, lastKnown.longitude)
                 } else {
                     _pickupAddress.value = "Fetching precise location..."
                     
                     // Request single update
                     val listener = object : android.location.LocationListener {
                         override fun onLocationChanged(loc: android.location.Location) {
                             updatePickupAddressFromLocation(context, loc.latitude, loc.longitude)
                             lm.removeUpdates(this) // Only need one
                         }
                         @Deprecated("Deprecated in Java")
                         override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                         override fun onProviderEnabled(provider: String) {}
                         override fun onProviderDisabled(provider: String) {}
                     }
                     
                     // Request updates from both providers to be safe
                     if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                         lm.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000L, 10f, listener)
                     }
                     if (lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                         lm.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000L, 10f, listener)
                     }
                 }
             } else {
                 _pickupAddress.value = "Permission required"
             }
        } catch (e: Exception) {
            e.printStackTrace()
             _pickupAddress.value = "Location error: ${e.message}"
        }
    }

    private val _routePoints = MutableStateFlow<List<org.osmdroid.util.GeoPoint>>(emptyList()) // Keep for backward compat if needed, or just use selected route
    
    // 🔥 NEW NAV STATES
    private val _availableRoutes = MutableStateFlow<List<com.example.floatingflavors.app.feature.user.data.tracking.OsrmRouteService.RouteResult>>(emptyList())
    val availableRoutes: StateFlow<List<com.example.floatingflavors.app.feature.user.data.tracking.OsrmRouteService.RouteResult>> = _availableRoutes
    
    private val _selectedRouteIndex = MutableStateFlow(0)
    val selectedRouteIndex: StateFlow<Int> = _selectedRouteIndex
    
    private val _isNavigationStarted = MutableStateFlow(false)
    val isNavigationStarted: StateFlow<Boolean> = _isNavigationStarted

    private val _destinationPoint = MutableStateFlow<org.osmdroid.util.GeoPoint?>(null)
    val destinationPoint: StateFlow<org.osmdroid.util.GeoPoint?> = _destinationPoint

    fun selectRoute(index: Int) {
        _selectedRouteIndex.value = index
    }

    fun toggleNavigation(start: Boolean) {
        _isNavigationStarted.value = start
    }
    
    /* ---------------- NAVIGATION ---------------- */

    private val _currentBearing = MutableStateFlow(0f)
    val currentBearing: StateFlow<Float> = _currentBearing

    private var tts: android.speech.tts.TextToSpeech? = null
    private var isTtsReady = false
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted
    
    // 🔥 NEW: Navigation Instructions
    private val _currentInstruction = MutableStateFlow<String?>(null)
    val currentInstruction: StateFlow<String?> = _currentInstruction
    
    private var currentRouteInstructions: List<com.example.floatingflavors.app.feature.user.data.tracking.TurnInstruction> = emptyList()
    private var currentStepIndex = 0

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun initTTS(context: Context) {
        if (tts == null) {
            tts = android.speech.tts.TextToSpeech(context.applicationContext) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    tts?.language = Locale.US
                }
            }
        }
    }
    
    // Speak instruction if not muted
    private fun speak(text: String) {
        if (isTtsReady && !isMuted.value) {
            tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Call this when we have both locations
    fun fetchRoute(startLat: Double, startLng: Double, destLat: Double, destLng: Double) {
        viewModelScope.launch {
            val start = org.osmdroid.util.GeoPoint(startLat, startLng)
            val end = org.osmdroid.util.GeoPoint(destLat, destLng)
            _destinationPoint.value = end
            
            try {
                val routes = com.example.floatingflavors.app.feature.user.data.tracking.OsrmRouteService.fetchMultipleRoutes(start, end)
                if (routes.isNotEmpty()) {
                    _availableRoutes.value = routes
                    selectRoute(0) // Default to first
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _currentGeoPoint = MutableStateFlow<org.osmdroid.util.GeoPoint?>(null)
    val currentGeoPoint: StateFlow<org.osmdroid.util.GeoPoint?> = _currentGeoPoint

    // 🔥 Calculate progress along instructions
    private fun updateNavigationInstruction(userLoc: org.osmdroid.util.GeoPoint?) {
        if (currentRouteInstructions.isEmpty() || userLoc == null) {
             _currentInstruction.value = "Proceed to route"
             return
        }
        
        // Simple Logic: If we are close to the next step's maneuver point, advance.
        // For real app: Project point onto polyline index.
        // Mock Implementation: Just show the text of current step index.
        val step = currentRouteInstructions.getOrNull(currentStepIndex)
        if (step != null) {
            val info = "${step.text} in ${step.distanceMeters}m"
            if (_currentInstruction.value != info) {
                _currentInstruction.value = info
                speak(step.text) // Voice Announcement
            }
            
            // Auto-advance logic (Very simplified: Assume we move 1 step per 100m - placeholder)
            // In reality, we need coordinate matching.
        } else {
             _currentInstruction.value = "Arriving at destination"
        }
    }

    private fun updatePickupAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        bearing: Float = 0f // 🔥 NEW ARGUMENT
    ) {
        // 🔥 Update State for Map
        val point = org.osmdroid.util.GeoPoint(latitude, longitude)
        _currentGeoPoint.value = point
        _currentBearing.value = bearing // 🔥 UPDATE BEARING
        
        if (_isNavigationStarted.value) {
             updateNavigationInstruction(point)
        }
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val str = listOfNotNull(
                        addr.subLocality,
                        addr.locality,
                        addr.adminArea
                    ).joinToString(", ")
                    _pickupAddress.value = str
                    
                     // 🔥 Auto-fetch route if destination is known
                    val dOrder = _order.value
                    if (dOrder?.deliveryAddress != null && _availableRoutes.value.isEmpty()) { // Fetch only if empty
                        try {
                           // We need destination coords. 
                           // For now, if API does not provide dest coords in 'OrderDto', we geocode deliveryAddress
                           val destList = geocoder.getFromLocationName(dOrder.deliveryAddress, 1)
                           if (!destList.isNullOrEmpty()) {
                               fetchRoute(latitude, longitude, destList[0].latitude, destList[0].longitude)
                           }
                        } catch(e: Exception) { e.printStackTrace() }
                    }

                }
            } catch (_: Exception) {
                _pickupAddress.value = "Loc: $latitude, $longitude"
            }
        }
    }

    /* ---------------- ACTIONS ---------------- */

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptOrderAndStartTracking(context: Context) {
        viewModelScope.launch {
            val res = repository.acceptOrder(orderId, deliveryPartnerId)
            if (res.success) {
                _acceptSuccess.value = true   // 🔥 REQUIRED
                loadOrderDetails()
                startTracking(context)
                startObservingPickup(context)
            }
        }
    }

    fun rejectOrder() {
        viewModelScope.launch {
            repository.rejectOrder(orderId)
            loadOrderDetails()
        }
    }

    fun markAsDelivered(context: Context) {
        viewModelScope.launch {
            repository.markAsDelivered(orderId)
            stopTracking(context)
            observeJob?.cancel()
            observeJob = null
            loadOrderDetails()
        }
    }

    fun clearAcceptState() {
        _acceptSuccess.value = false
    }

    /* ---------------- TRACKING SERVICE ---------------- */

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTracking(context: Context) {
        val intent = Intent(context, DeliveryLocationUpdateService::class.java).apply {
            action = "START_TRACKING"
            putExtra("ORDER_ID", orderId)
            putExtra("DELIVERY_PARTNER_ID", deliveryPartnerId)
        }
        context.startForegroundService(intent)
    }

    private fun stopTracking(context: Context) {
        context.stopService(Intent(context, DeliveryLocationUpdateService::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun resumeTrackingIfNeeded(context: Context) {
        if (_order.value?.status == "OUT_FOR_DELIVERY") {
            startTracking(context)
            startObservingPickup(context)
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}
