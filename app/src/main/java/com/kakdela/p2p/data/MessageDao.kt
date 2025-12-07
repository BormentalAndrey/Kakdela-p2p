package com.kakdela.p2p.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp")
    fun getMessages(chatId: String): Flow<List<MessageEntity>>

    @Insert
    suspend fun insert(message: MessageEntity)
}
