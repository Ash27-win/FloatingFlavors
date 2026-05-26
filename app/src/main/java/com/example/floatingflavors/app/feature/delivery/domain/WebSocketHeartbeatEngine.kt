package com.example.floatingflavors.app.feature.delivery.domain

import android.util.Log
import kotlinx.coroutines.*

/**
 * Enterprise WebSocket Heartbeat Ping/Pong Engine — Feature 19
 *
 * Problem: WebSocket connections over mobile networks drop silently in Android Doze
 * mode or behind restrictive carrier NAT tables. The app believes the socket is live
 * while the backend has already timed it out, causing GPS updates to disappear from
 * the dispatcher map without any error in the rider app.
 *
 * Solution:
 *  - The heartbeat engine sends a lightweight PING message every [PING_INTERVAL_MS].
 *  - The backend must echo a PONG message within [PONG_TIMEOUT_MS].
 *  - If no PONG is received in time:
 *      1. [onConnectionDead] is called → triggers [NavigationPhase.OfflineMode] in ViewModel.
 *      2. GPS updates are no longer pushed to the server (they queue to SQLite instead).
 *      3. Reconnection is attempted with exponential backoff (2s → 4s → 8s → max 60s).
 *  - When the connection is re-established, [onConnectionRestored] fires and the offline
 *    SQLite queue is flushed back to the server in order.
 *
 * Usage:
 *   val heartbeat = WebSocketHeartbeatEngine(
 *       onPingSend      = { webSocket.send("{\"type\":\"PING\"}") },
 *       onConnectionDead     = { viewModel.setOfflineMode(true) },
 *       onConnectionRestored = { viewModel.setOfflineMode(false) }
 *   )
 *   heartbeat.start()
 *   // On incoming message: if (message.contains("PONG")) heartbeat.onPongReceived()
 *   heartbeat.stop()
 */
class WebSocketHeartbeatEngine(
    private val onPingSend: suspend () -> Unit,
    private val onConnectionDead: () -> Unit,
    private val onConnectionRestored: () -> Unit
) {
    companion object {
        private const val TAG              = "WsHeartbeat"
        private const val PING_INTERVAL_MS = 15_000L  // Send ping every 15 seconds
        private const val PONG_TIMEOUT_MS  = 8_000L   // Must receive pong within 8 seconds
        private const val MAX_BACKOFF_MS   = 60_000L  // Max reconnect interval
        private const val INITIAL_BACKOFF  = 2_000L   // Start retry at 2 seconds
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var pingJob: Job?    = null
    private var timeoutJob: Job? = null

    @Volatile private var isPongPending     = false
    @Volatile private var isConnected       = true
    @Volatile private var isRunning         = false
    @Volatile private var reconnectBackoff  = INITIAL_BACKOFF

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Starts the heartbeat loop. Call after the WebSocket handshake is confirmed.
     */
    fun start() {
        if (isRunning) return
        isRunning   = true
        isConnected = true
        reconnectBackoff = INITIAL_BACKOFF
        Log.i(TAG, "Heartbeat engine STARTED.")
        schedulePing()
    }

    /**
     * Call this every time a PONG message is received from the backend WebSocket.
     */
    fun onPongReceived() {
        if (!isPongPending) return
        isPongPending = false
        timeoutJob?.cancel()

        if (!isConnected) {
            // Connection was dead but PONG arrived — we're back online
            isConnected      = true
            reconnectBackoff = INITIAL_BACKOFF
            Log.i(TAG, "✅ Connection RESTORED.")
            onConnectionRestored()
        }
    }

    /**
     * Stops all heartbeat jobs. Call in Service.onDestroy() or WebSocket.onClosed().
     */
    fun stop() {
        isRunning = false
        pingJob?.cancel()
        timeoutJob?.cancel()
        Log.i(TAG, "Heartbeat engine STOPPED.")
    }

    // ── Internal Loop ─────────────────────────────────────────────────────────

    private fun schedulePing() {
        pingJob = scope.launch {
            while (isRunning) {
                delay(PING_INTERVAL_MS)
                sendPingAndAwaitPong()
            }
        }
    }

    private fun sendPingAndAwaitPong() {
        scope.launch {
            try {
                isPongPending = true
                // Log.d(TAG, "→ PING sent.")
                onPingSend()

                // Start a timeout watchdog for this PONG
                timeoutJob?.cancel()
                timeoutJob = scope.launch {
                    delay(PONG_TIMEOUT_MS)
                    if (isPongPending) {
                        // No PONG received — connection is dead
                        isPongPending = false
                        handleConnectionDead()
                    }
                }
            } catch (e: Exception) {
                // Log.e(TAG, "PING send failed: ${e.message}")
                handleConnectionDead()
            }
        }
    }

    private fun handleConnectionDead() {
        if (isConnected) {
            // First time we detect the drop — notify the ViewModel
            isConnected = false
            Log.w(TAG, "💔 Connection DEAD. Switching to Offline Mode.")
            onConnectionDead()
        }

        // Schedule a reconnect attempt with exponential backoff
        scope.launch {
            // Log.i(TAG, "Attempting reconnect in ${reconnectBackoff / 1000}s...")
            delay(reconnectBackoff)
            reconnectBackoff = (reconnectBackoff * 2).coerceAtMost(MAX_BACKOFF_MS)

            // Send a PING to probe if the connection has recovered
            sendPingAndAwaitPong()
        }
    }
}
