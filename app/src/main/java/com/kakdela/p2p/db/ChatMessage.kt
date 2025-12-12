package com.kakdela.p2p.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val receiverId: String,
    val type: String,     // text, file, voice
    val content: String,  // text or file path
    val timestamp: Long   // System.currentTimeMillis()
)
