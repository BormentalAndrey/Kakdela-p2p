// app/src/main/java/com/kakdela/p2p/webrtc/FileTransferManager.kt
package com.kakdela.p2p.webrtc

import android.content.Context
import android.net.Uri
import android.util.Log
import com.kakdela.p2p.crypto.CryptoManager
import com.kakdela.p2p.model.ChatMessage
import com.kakdela.p2p.model.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

object FileTransferManager {

    private const val TAG = "FileTransferManager"

    /** Отправка обычного текста */
    fun sendText(peerId: String, text: String) {
        val publicKey = ContactsRepository.getById(peerId)?.publicKeyHex ?: return
        val encrypted = CryptoManager.encrypt(text, publicKey)
        WebRtcManager.send(peerId, ChatMessage.text(encrypted))
    }

    /** Отправка фото, видео, файла */
    fun sendFile(context: Context, peerId: String, uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val bytes = inputStream.readBytes()
                inputStream.close()

                val publicKey = ContactsRepository.getById(peerId)?.publicKeyHex ?: return@launch
                val encryptedBytes = CryptoManager.encrypt(bytes, publicKey)

                val fileName = uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"

                val message = ChatMessage.file(
                    fileId = UUID.randomUUID().toString(),
                    encryptedData = encryptedBytes,
                    fileName = fileName,
                    fileUri = uri.toString()
                )

                WebRtcManager.send(peerId, message)
                Log.d(TAG, "Файл зашифрован и отправлен: $fileName")
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка отправки файла", e)
            }
        }
    }

    /** Отправка голосового сообщения */
    fun sendVoice(peerId: String, audioFile: File) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bytes = audioFile.readBytes()
                val publicKey = ContactsRepository.getById(peerId)?.publicKeyHex ?: return@launch
                val encrypted = CryptoManager.encrypt(bytes, publicKey)

                val message = ChatMessage.voice(
                    encryptedData = encrypted,
                    fileName = audioFile.name
                )

                WebRtcManager.send(peerId, message)
                audioFile.delete() // очищаем кэш
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка отправки голосового", e)
            }
        }
    }
}
