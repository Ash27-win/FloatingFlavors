package com.example.floatingflavors.app.chatbot.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAll(): List<ChatEntity>

    @Insert
    suspend fun insert(message: ChatEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clear()
}

