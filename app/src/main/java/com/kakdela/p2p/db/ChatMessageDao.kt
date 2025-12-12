package com.kakdela.p2p.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(message: ChatMessage)

    @Query("SELECT * FROM ChatMessage WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun observeChat(chatId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM ChatMessage ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ChatMessage>>
}
