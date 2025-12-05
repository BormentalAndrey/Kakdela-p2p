package com.kakdela.p2p.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val remoteId: String? = null,
    val chatId: String,
    val senderId: String,
    val text: String? = null,
    val filePath: String? = null,        // ← путь к фото/видео/файлу/голосовому
    val fileName: String? = null,        // ← оригинальное имя файла
    val fileSize: Long = 0L,             // ← размер в байтах
    val type: String = "text",           // ← text, image, video, file, voice
    val duration: Long = 0L,             // ← длительность голосового (в мс)
    val isEncrypted: Boolean = true,     // ← всегда шифруется
    val timestamp: Long = System.currentTimeMillis(),
    val delivered: Boolean = false,
    val synced: Boolean = false
)
