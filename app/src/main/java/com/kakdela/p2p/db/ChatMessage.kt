package com.kakdela.p2p.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,     // идентификатор чата/peer
    val author: String,     // who sent
    val text: String,
    val type: String = "text", // text/voice/file
    val timestamp: Long = System.currentTimeMillis()
)
