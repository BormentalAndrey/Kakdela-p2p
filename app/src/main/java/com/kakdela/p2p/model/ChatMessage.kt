// app/src/main/java/com/kakdela/p2p/model/ChatMessage.kt
package com.kakdela.p2p.model

import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String? = null,
    val fileUri: String? = null,           // путь к файлу или "voice://..."
    val fileName: String? = null,
    val encryptedData: ByteArray? = null,  // для отправки по сети
    val isFromMe: Boolean,
    val time: String = getCurrentTime(),

    // Автоопределение типа
    val isImage: Boolean = fileUri?.let {
        it.endsWith(".jpg", true) || it.endsWith(".jpeg", true) ||
        it.endsWith(".png", true) || it.endsWith(".webp", true)
    } == true,

    val isVideo: Boolean = fileUri?.let {
        it.endsWith(".mp4", true) || it.endsWith(".mov", true) ||
        it.endsWith(".avi", true) || it.endsWith(".mkv", true)
    } == true,

    val isVoice: Boolean = fileUri?.startsWith("voice://") == true,

    val isFile: Boolean = fileUri != null && !isImage && !isVideo && !isVoice
) {
    companion object {
        fun text(text: String, isFromMe: Boolean = true) =
            ChatMessage(text = text, isFromMe = isFromMe)

        fun image(uri: String, isFromMe: Boolean = true) =
            ChatMessage(fileUri = uri, isFromMe = isFromMe)

        fun video(uri: String, isFromMe: Boolean = true) =
            ChatMessage(fileUri = uri, isFromMe = isFromMe)

        fun voice(fileName: String, isFromMe: Boolean = true) =
            ChatMessage(fileUri = "voice://$fileName", fileName = fileName, isFromMe = isFromMe)

        fun file(uri: String, name: String, isFromMe: Boolean = true) =
            ChatMessage(fileUri = uri, fileName = name, isFromMe = isFromMe)

        private fun getCurrentTime(): String =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    override fun equals(other: Any?): Boolean = other is ChatMessage && other.id == id
    override fun hashCode(): Int = id.hashCode()
}
