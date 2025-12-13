package com.kakdela.p2p.signaling

import com.kakdela.p2p.webrtc.WebRtcManager
import org.webrtc.SessionDescription

class SignalingClient(private val webRtcManager: WebRtcManager) {

    fun sendOffer(peerId: String) {
        webRtcManager.createOffer { sdp ->
            // Генерировать QR с SDP
            val qrData = "sdp:${sdp.description}"
            // Показать QR в UI
        }
    }

    fun receiveSdpFromQr(sdpString: String) {
        val type = if (sdpString.startsWith("offer")) SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER
        val sdp = SessionDescription(type, sdpString.substringAfter(":"))
        webRtcManager.setRemoteDescription(sdp)
    }

    // Для Wi-Fi Direct: использовать sockets для обмена SDP
}
