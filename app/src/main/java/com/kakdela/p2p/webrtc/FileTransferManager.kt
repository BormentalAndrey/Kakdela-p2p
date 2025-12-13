package com.kakdela.p2p.webrtc

import android.content.Context
import android.net.Uri

object FileTransferManager {
    fun sendFile(peerId: String, uri: Uri, context: Context) {
        // Заглушка: отправка файла через data channel WebRTC
    }

    fun sendText(peerId: String, text: String) {
        // Заглушка: отправка текста
    }

    fun sendVoice(peerId: String, voice: ByteArray) {
        // Заглушка: отправка голосового
    }
}
