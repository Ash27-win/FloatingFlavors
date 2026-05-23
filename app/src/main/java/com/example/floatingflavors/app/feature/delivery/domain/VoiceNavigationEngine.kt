package com.example.floatingflavors.app.feature.delivery.domain

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.*
import org.osmdroid.util.GeoPoint
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Enterprise-grade Voice Navigation Engine — Feature 18 Enhanced
 *
 * Previous behaviour: Instructions were triggered by arbitrary conditions (timers, flags).
 * The rider would hear "Turn left" only when directly at the junction, giving zero reaction time.
 *
 * Feature 18 Enhancement — Turf Distance Pre-emption:
 *  - The engine now receives the NEXT navigation step's coordinates and instruction text.
 *  - On each GPS update it measures the distance from the rider to the next turn.
 *  - It pre-announces instructions at THREE distances:
 *      • 500 m away → "In 500 meters, turn left onto Beach Road"
 *      • 200 m away → "In 200 meters, turn left onto Beach Road"
 *      •  30 m away → "Turn left NOW"
 *  - Each announcement fires exactly once per turn using a spoken-set guard.
 *  - Existing cooldown + queue logic is preserved for all other instruction types.
 *
 * Other features preserved:
 *  - Thread-safe ConcurrentLinkedQueue for non-urgent instructions.
 *  - 30-second per-instruction cooldown to prevent repetition spam.
 *  - speakUrgent() flushes queue and bypasses cooldowns.
 */
class VoiceNavigationEngine(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VoiceEngine"
        private const val COOLDOWN_MS = 30_000L  // 30s per-instruction cooldown

        // Pre-emption thresholds (meters)
        private const val ANNOUNCE_500M =  500.0
        private const val ANNOUNCE_200M =  200.0
        private const val ANNOUNCE_NOW  =   30.0
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val speechQueue          = ConcurrentLinkedQueue<String>()
    private val instructionCooldowns = mutableMapOf<String, Long>()
    private val engineScope          = CoroutineScope(Dispatchers.Default + Job())

    // ── Feature 18: Next-Turn Awareness ──────────────────────────────────────
    private var nextTurnPoint: GeoPoint?  = null
    private var nextTurnInstruction: String = ""

    /** Tracks which pre-emption thresholds have fired for the current turn */
    private val spokenThresholds = mutableSetOf<String>()  // "500", "200", "now"

    init {
        tts = TextToSpeech(context, this)
        startQueueProcessor()
    }

    override fun onInit(status: Int) {
        isInitialized = status == TextToSpeech.SUCCESS
        if (!isInitialized) Log.e(TAG, "TTS Initialization failed.")
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Update the next navigation turn that the voice engine should pre-announce.
     * Call this whenever the active route's next step changes.
     *
     * @param point        GeoPoint of the next turn junction.
     * @param instruction  Human-readable instruction, e.g. "Turn left onto Beach Road"
     */
    fun setNextTurn(point: GeoPoint, instruction: String) {
        if (nextTurnInstruction != instruction) {
            nextTurnPoint       = point
            nextTurnInstruction = instruction
            spokenThresholds.clear()  // Reset thresholds for the new turn
            Log.i(TAG, "Next turn set: '$instruction' at (${point.latitude}, ${point.longitude})")
        }
    }

    /**
     * Call on every GPS update with the rider's current position.
     * The engine will automatically fire pre-emptive voice instructions
     * at 500m, 200m, and 30m from the next turn.
     */
    fun onLocationUpdate(riderPosition: GeoPoint) {
        val turnPoint = nextTurnPoint ?: return
        val distance  = riderPosition.distanceToAsDouble(turnPoint)

        when {
            distance <= ANNOUNCE_NOW  && "now"  !in spokenThresholds -> {
                spokenThresholds.add("now")
                speakUrgent(nextTurnInstruction)
                Log.i(TAG, "NOW announcement: '$nextTurnInstruction'")
            }
            distance <= ANNOUNCE_200M && "200"  !in spokenThresholds -> {
                spokenThresholds.add("200")
                enqueueInstruction("In 200 meters, $nextTurnInstruction", overrideCooldown = true)
                Log.i(TAG, "200m announcement: '$nextTurnInstruction'")
            }
            distance <= ANNOUNCE_500M && "500"  !in spokenThresholds -> {
                spokenThresholds.add("500")
                enqueueInstruction("In 500 meters, $nextTurnInstruction", overrideCooldown = true)
                Log.i(TAG, "500m announcement: '$nextTurnInstruction'")
            }
        }
    }

    /**
     * Enqueues a non-urgent instruction.
     * Ignored if the exact same text was spoken within [COOLDOWN_MS].
     */
    fun enqueueInstruction(text: String, overrideCooldown: Boolean = false) {
        val now      = System.currentTimeMillis()
        val lastSpoken = instructionCooldowns[text] ?: 0L
        if (overrideCooldown || (now - lastSpoken > COOLDOWN_MS)) {
            speechQueue.add(text)
            instructionCooldowns[text] = now
        }
    }

    /**
     * Immediately flushes the queue and speaks an urgent alert.
     * Use for: deviation detected, arrival, SOS, battery warning.
     */
    fun speakUrgent(text: String) {
        if (!isInitialized) return
        speechQueue.clear()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun startQueueProcessor() {
        engineScope.launch {
            while (isActive) {
                if (isInitialized && speechQueue.isNotEmpty() && tts?.isSpeaking == false) {
                    val next = speechQueue.poll()
                    if (next != null) tts?.speak(next, TextToSpeech.QUEUE_ADD, null, null)
                }
                delay(500L)
            }
        }
    }

    fun shutdown() {
        engineScope.cancel()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
