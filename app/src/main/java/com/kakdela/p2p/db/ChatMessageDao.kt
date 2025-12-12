package com.kakdela.p2p.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Insert
    suspend fun insert(message: ChatMessage)

    @Query("SELECT * FROM ChatMessage WHERE senderId = :user OR receiverId = :user ORDER BY timestamp ASC")
    fun getMessages(user: String): Flow<List<ChatMessage>>
}
