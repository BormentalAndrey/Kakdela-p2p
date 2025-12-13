package com.kakdela.p2p.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val peerId: String,
    val content: String,
    val type: String, // text, image, voice
    val isSent: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Int? = null // for voice
)
