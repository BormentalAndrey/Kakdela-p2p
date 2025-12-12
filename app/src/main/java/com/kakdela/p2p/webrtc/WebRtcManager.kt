package com.kakdela.p2p.webrtc

import android.content.Context

object WebRtcManager {

    fun initialize(context: Context) {
        // TODO: Initialize WebRTC PeerConnectionFactory
    }

    fun initiateConnection(peerId: String, publicKey: String, iceServersJson: String) {
        // TODO: Signaling + PeerConnection createOffer
    }

    fun receiveOffer(peerId: String, offerSdp: String) {
        // TODO: setRemoteDescription + createAnswer
    }

    fun receiveAnswer(peerId: String, answerSdp: String) {
        // TODO: setRemoteDescription
    }

    fun addIceCandidate(peerId: String, candidate: String) {
        // TODO: Add ICE candidate
    }
}
