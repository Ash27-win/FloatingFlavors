package com.example.floatingflavors.app

import android.app.Application
import android.util.Log
import com.example.floatingflavors.app.feature.delivery.domain.TileCacheManager
import com.example.floatingflavors.app.feature.delivery.domain.TileCleanupWorker

/**
 * Custom Application class for Floating Flavors.
 *
 * Responsibilities (Feature 8):
 *  - Configures osmdroid's tile cache hard cap (50MB) on first launch.
 *  - Schedules the nightly [TileCleanupWorker] to evict stale tile files.
 *
 * Registered in AndroidManifest.xml via android:name=".app.FloatingFlavorsApp"
 */
class FloatingFlavorsApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Feature 8: Configure tile cache limits before any MapView is created
        TileCacheManager.configure(this)
        Log.i("FloatingFlavorsApp", "Tile cache configured.")

        // Feature 8: Schedule nightly tile cleanup (idempotent — safe to call on every launch)
        TileCleanupWorker.schedule(this)
        Log.i("FloatingFlavorsApp", "TileCleanupWorker scheduled.")
    }
}
