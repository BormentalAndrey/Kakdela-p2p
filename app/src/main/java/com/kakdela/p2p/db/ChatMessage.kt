package com.kakdela.p2p.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val author: String,
    val text: String,
    val type: String = "text",
    val timestamp: Long = System.currentTimeMillis()
)
