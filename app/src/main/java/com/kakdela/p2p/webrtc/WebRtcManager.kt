package com.kakdela.p2p.webrtc

import org.webrtc.*

class WebRtcManager(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val crypto: CryptoManager
) {

    var dataChannel: DataChannel? = null
    var theirPublicKeyHex: String? = null

    fun encryptAndSend(bytes: ByteArray) {
        theirPublicKeyHex ?: return
        val encrypted = crypto.encrypt(bytes, theirPublicKeyHex!!)
        dataChannel?.send(DataChannel.Buffer(
            java.nio.ByteBuffer.wrap(encrypted), false
        ))
    }
}
