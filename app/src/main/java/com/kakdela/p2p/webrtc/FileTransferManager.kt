package com.kakdela.p2p.webrtc

import android.content.Context
import android.net.Uri
import com.kakdela.p2p.crypto.CryptoManager
import org.webrtc.DataChannel

object FileTransferManager {

    private val dataChannels = mutableMapOf<String, DataChannel>()

    fun addDataChannel(peerId: String, dataChannel: DataChannel) {
        dataChannels[peerId] = dataChannel
    }

    fun sendText(peerId: String, text: String) {
        val nonce = CryptoManager.generateNonce()
        val cipher = CryptoManager.encrypt(text.toByteArray(), nonce, Key.fromHexString(/* receiver public */), CryptoManager.myKeyPair.secretKey)
        val buffer = DataChannel.Buffer(ByteBuffer.wrap(cipher), false)
        dataChannels[peerId]?.send(buffer)
    }

    fun sendFile(peerId: String, uri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: return
        sendText(peerId, String(bytes)) // Для примера; для больших файлов - chunks
    }

    fun sendVoice(peerId: String, voice: ByteArray) {
        sendFile(peerId, Uri.fromFile(File(/* path */)), context) // Аналогично
    }
}
