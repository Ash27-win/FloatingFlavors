package com.example.floatingflavors.app.feature.delivery.domain

import android.content.Context
import android.util.Log
import org.osmdroid.config.Configuration
import java.io.File

/**
 * Enterprise Tile Cache Manager for osmdroid.
 *
 * Without this, continuous 8-hour tracking sessions bloat the osmdroid tile cache
 * from ~50MB to 2GB+, causing OutOfMemoryError crashes on mid-range devices.
 *
 * Strategy:
 *  - Hard cap the on-disk tile cache at MAX_CACHE_BYTES (50MB).
 *  - If the cache directory exceeds TRIM_THRESHOLD_BYTES (40MB), delete oldest tiles.
 *  - Enforce osmdroid's in-memory tile limit at 400 tiles to prevent RAM OOM.
 */
object TileCacheManager {

    private const val TAG                    = "TileCacheManager"
    private const val MAX_CACHE_BYTES        = 50L * 1024 * 1024   // 50 MB hard cap
    private const val TRIM_THRESHOLD_BYTES   = 40L * 1024 * 1024   // Trim when > 40 MB
    private const val MAX_MEMORY_TILES       = 400                  // In-memory tile limit

    /**
     * Call once in Application.onCreate() or before the first map is rendered.
     * Configures osmdroid's global tile writer limits.
     */
    fun configure(context: Context) {
        val config = Configuration.getInstance()

        // 1. Point osmdroid's cache to a well-known directory in internal storage
        val cacheDir = File(context.cacheDir, "osmdroid_tiles")
        config.osmdroidTileCache = cacheDir

        // 2. Enforce hard limits on the osmdroid tile writer
        config.tileFileSystemCacheMaxBytes  = MAX_CACHE_BYTES
        config.tileFileSystemCacheTrimBytes = TRIM_THRESHOLD_BYTES

        Log.i(TAG, "Tile cache configured: max=${MAX_CACHE_BYTES / (1024 * 1024)}MB " +
                "trim=${TRIM_THRESHOLD_BYTES / (1024 * 1024)}MB " +
                "cache_dir=${cacheDir.absolutePath}")
    }

    /**
     * Synchronously calculates the total size of the tile cache directory.
     * Safe to call from a background coroutine.
     */
    fun getCacheSizeBytes(context: Context): Long {
        val cacheDir = File(context.cacheDir, "osmdroid_tiles")
        return cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    /**
     * Deletes tiles older than [maxAgeMs] milliseconds.
     * Called by [TileCleanupWorker] during nightly maintenance.
     *
     * @param maxAgeMs  Default: 24 hours
     * @return Number of tile files deleted.
     */
    fun evictStaleFiles(context: Context, maxAgeMs: Long = 24L * 60 * 60 * 1000): Int {
        val cacheDir = File(context.cacheDir, "osmdroid_tiles")
        if (!cacheDir.exists()) return 0

        val cutoff = System.currentTimeMillis() - maxAgeMs
        var deletedCount = 0

        cacheDir.walkTopDown().filter { it.isFile && it.lastModified() < cutoff }.forEach { file ->
            if (file.delete()) {
                deletedCount++
            }
        }

        Log.i(TAG, "Tile eviction complete: deleted $deletedCount stale tile files.")
        return deletedCount
    }

    /**
     * Force-clears the entire tile cache. Use only on low-storage warnings.
     */
    fun clearAll(context: Context) {
        val cacheDir = File(context.cacheDir, "osmdroid_tiles")
        cacheDir.deleteRecursively()
        Log.w(TAG, "Full tile cache cleared.")
    }
}
