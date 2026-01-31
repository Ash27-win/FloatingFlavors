package com.example.floatingflavors.app.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.floatingflavors.app.feature.notification.data.local.NotificationDao
import com.example.floatingflavors.app.feature.notification.data.local.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

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
