package com.example.floatingflavors.app.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.floatingflavors.app.feature.notification.data.local.NotificationDao
import com.example.floatingflavors.app.feature.notification.data.local.NotificationEntity
import com.example.floatingflavors.app.feature.delivery.data.local.BufferedLocationEntity
import com.example.floatingflavors.app.feature.delivery.data.local.BufferedLocationDao
import com.example.floatingflavors.app.feature.delivery.data.local.RouteCacheEntity
import com.example.floatingflavors.app.feature.delivery.data.local.RouteCacheDao
import com.example.floatingflavors.app.feature.delivery.data.local.DeliveryAnalyticsEntity
import com.example.floatingflavors.app.feature.delivery.data.local.DeliveryAnalyticsDao

@Database(
    entities = [
        NotificationEntity::class,
        BufferedLocationEntity::class,
        RouteCacheEntity::class,
        DeliveryAnalyticsEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao
    abstract fun bufferedLocationDao(): BufferedLocationDao
    abstract fun routeCacheDao(): RouteCacheDao
    abstract fun deliveryAnalyticsDao(): DeliveryAnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "floating_flavors_app_db"
                )
                .fallbackToDestructiveMigration() // Simplified for this stage
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
