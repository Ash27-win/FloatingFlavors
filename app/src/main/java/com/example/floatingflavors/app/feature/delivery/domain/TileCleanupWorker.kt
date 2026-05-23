package com.example.floatingflavors.app.feature.delivery.domain

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Nightly WorkManager job to evict stale osmdroid tile cache files.
 *
 * Runs once every 24 hours (respecting Doze mode constraints).
 * Deletes any tile file older than 24 hours to keep the cache under 50MB.
 *
 * Schedule from Application.onCreate() using [TileCleanupWorker.schedule].
 */
class TileCleanupWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG        = "TileCleanupWorker"
        private const val WORK_NAME  = "floating_flavors_tile_cleanup"

        /**
         * Call once from Application.onCreate() to register the nightly cleanup job.
         * Uses [ExistingPeriodicWorkPolicy.KEEP] so it doesn't re-register on every cold start.
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TileCleanupWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

            Log.i(TAG, "Nightly tile cleanup worker scheduled.")
        }
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "Tile cleanup job started.")

        return try {
            val deleted = TileCacheManager.evictStaleFiles(context)
            val remainingBytes = TileCacheManager.getCacheSizeBytes(context)
            val remainingMb = remainingBytes / (1024 * 1024)

            Log.i(TAG, "Cleanup complete: $deleted files deleted, ${remainingMb}MB remaining.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Tile cleanup failed: ${e.message}")
            Result.retry()
        }
    }
}
