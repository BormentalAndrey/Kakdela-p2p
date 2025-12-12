package com.kakdela.p2p.webrtc

import android.content.Context

object WebRtcManager {

    fun initialize(context: Context) {
        // TODO: PeerConnectionFactory initialization
    }

    fun initiateConnection(peerId: String, publicKey: String, iceServersJson: String) {
        // TODO: signaling sendOffer
    }

    fun receiveOffer(peerId: String, offerSdp: String) {
        // TODO: set remote offer, createAnswer()
    }

    fun receiveAnswer(peerId: String, answerSdp: String) {
        // TODO: set remote answer
    }

    fun addIceCandidate(peerId: String, candidate: String) {
        // TODO: add ice candidate to PeerConnection
    }
}
