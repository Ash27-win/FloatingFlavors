package com.example.floatingflavors.app.feature.delivery.domain

import android.util.Log
import java.util.PriorityQueue

/**
 * Dispatcher Live Sync Engine — Timestamp-Based GPS Ordering
 *
 * Problem: WebSocket packets from the rider's device arrive at the dispatcher map
 * out of order due to network jitter. This causes the dispatcher to see the rider
 * "jump backward" on the map every few seconds.
 *
 * Solution: Buffer incoming GPS packets into a [PriorityQueue] sorted by timestamp.
 * Only emit a packet if its timestamp is strictly newer than the last rendered frame.
 * This guarantees monotonically increasing position updates on the dispatcher side.
 *
 * Usage (Dispatcher side):
 *   val engine = DispatcherSyncEngine()
 *   engine.onIncomingPacket(lat, lng, timestamp)
 *   // Subscribe to engine.orderedUpdates for the clean, deduplicated stream
 */
class DispatcherSyncEngine {

    companion object {
        private const val TAG = "DispatcherSyncEngine"

        // Maximum number of packets to hold in the buffer before force-flushing
        private const val MAX_BUFFER_SIZE = 50

        // Accept packets no older than 10 seconds behind the latest timestamp
        private const val MAX_LAG_MS = 10_000L
    }

    /**
     * Represents a single GPS packet received from the rider's device.
     */
    data class GpsPacket(
        val riderId: Int,
        val lat: Double,
        val lng: Double,
        val timestampMs: Long,
        val speedKmh: Double = 0.0,
        val bearing: Float = 0f
    ) : Comparable<GpsPacket> {
        // Ascending order: lowest timestamp = highest priority (process first)
        override fun compareTo(other: GpsPacket): Int =
            compareValuesBy(this, other) { it.timestampMs }
    }

    // The priority queue sorts by timestamp ascending
    private val packetQueue = PriorityQueue<GpsPacket>()

    // Timestamp of the last packet that was rendered on the dispatcher map
    private var lastRenderedTimestampMs: Long = 0L

    /**
     * Incoming WebSocket packet handler.
     * Inserts the packet into the priority queue.
     *
     * @return The [GpsPacket] to render right now, or null if we should wait.
     */
    fun onIncomingPacket(packet: GpsPacket): GpsPacket? {
        val now = System.currentTimeMillis()

        // 1. Hard-reject packets that are impossibly old (network replay attack / ghost packets)
        if (packet.timestampMs < now - MAX_LAG_MS) {
            Log.w(TAG, "Rejected stale packet: rider=${packet.riderId}, lag=${now - packet.timestampMs}ms")
            return null
        }

        // 2. Buffer the incoming packet
        packetQueue.add(packet)

        // 3. Force-flush if buffer is overflowing (prevents memory leak under high jitter)
        if (packetQueue.size > MAX_BUFFER_SIZE) {
            Log.w(TAG, "Buffer overflow: force-flushing ${packetQueue.size} packets.")
            return flushOldest()
        }

        // 4. Only emit if the next packet in queue is newer than last rendered
        val next = packetQueue.peek() ?: return null
        return if (next.timestampMs > lastRenderedTimestampMs) {
            flushOldest()
        } else {
            null
        }
    }

    /**
     * Polls the oldest (lowest timestamp) packet from the queue and marks it as rendered.
     */
    private fun flushOldest(): GpsPacket? {
        val packet = packetQueue.poll() ?: return null
        lastRenderedTimestampMs = packet.timestampMs
        Log.d(TAG, "Rendered packet: rider=${packet.riderId}, ts=${packet.timestampMs}")
        return packet
    }

    /**
     * Returns all currently buffered packets that are ready to render (in order).
     * Call this periodically (every 500ms) to drain the queue.
     */
    fun drainReadyPackets(): List<GpsPacket> {
        val result = mutableListOf<GpsPacket>()
        while (packetQueue.isNotEmpty()) {
            val next = packetQueue.peek() ?: break
            if (next.timestampMs > lastRenderedTimestampMs) {
                result.add(flushOldest()!!)
            } else {
                break
            }
        }
        return result
    }

    /**
     * Resets the engine. Call when switching to a different rider on the dispatcher map.
     */
    fun reset() {
        packetQueue.clear()
        lastRenderedTimestampMs = 0L
    }
}
