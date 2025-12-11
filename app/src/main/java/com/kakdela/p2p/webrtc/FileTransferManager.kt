// app/src/main/java/com/kakdela/p2p/webrtc/FileTransferManager.kt
package com.kakdela.p2p.webrtc

import android.content.Context
import android.net.Uri
import com.kakdela.p2p.crypto.CryptoManager
import com.kakdela.p2p.model.MessageType
import com.kakdela.p2p.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object FileTransferManager {

    fun sendText(peerId: String, text: String) {
        val encrypted = CryptoManager.encrypt(text, getPublicKeyForPeer(peerId))
        WebRtcManager.send(peerId, ChatMessage.text(encrypted))
    }

    fun sendFile(context: Context, peerId: String, uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
            val bytes = inputStream.readBytes()
            inputStream.close()

            val encrypted = CryptoManager.encrypt(bytes, getPublicKeyForPeer(peerId))

            val fileId = "\( {System.currentTimeMillis()}_ \){uri.lastPathSegment ?: "file"}"
            WebRtcManager.send(peerId, ChatMessage.file(fileId, encrypted, uri.lastPathSegment ?: "file"))
        }
    }

    private fun getPublicKeyForPeer(peerId: String): String {
        return ContactsRepository.getById(peerId)?.publicKeyHex ?: ""
    }
}
