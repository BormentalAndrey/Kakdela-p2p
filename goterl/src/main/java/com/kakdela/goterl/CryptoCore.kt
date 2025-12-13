package com.kakdela.p2p.webrtc

import android.content.Context
import org.webrtc.*

class WebRtcManager(private val context: Context) {
    private var peerConnection: PeerConnection? = null

    fun initiateConnection(peerId: String, publicKeyHex: String, iceServers: List<String>) {
        // Заглушка: создание PeerConnection, offer, etc.
    }
}
