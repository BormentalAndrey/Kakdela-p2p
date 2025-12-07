package com.kakdela.p2p.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp")
    fun getAll(): Flow<List<MessageEntity>>

    @Insert
    suspend fun insert(message: MessageEntity)
}
