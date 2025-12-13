package com.kakdela.p2p.webrtc

import android.content.Context
import org.webrtc.*

class WebRtcManager(private val context: Context) {

    private val peerConnectionFactory: PeerConnectionFactory
    private val peerConnections = mutableMapOf<String, PeerConnection>()

    init {
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions())
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun initiateConnection(peerId: String, publicKeyHex: String, iceServers: List<String>) {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers.map { PeerConnection.IceServer.builder(it).createIceServer() })
        val peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                // Отправить candidate через signaling (QR или socket)
            }
            override fun onAddStream(stream: MediaStream) {
                // Обработать поток
            }
            override fun onDataChannel(dataChannel: DataChannel) {
                FileTransferManager.addDataChannel(peerId, dataChannel)
            }
            // Другие методы...
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onRenegotiationNeeded() {}
            override fun onTrack(p0: RtpTransceiver?) {}
        })

        peerConnections[peerId] = peerConnection!!

        val dataChannel = peerConnection.createDataChannel("data", DataChannel.Init())
        FileTransferManager.addDataChannel(peerId, dataChannel)

        createOffer(peerId)
    }

    private fun createOffer(peerId: String) {
        peerConnections[peerId]?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnections[peerId]?.setLocalDescription(sdp, object : SdpObserver {
                    override fun onSetSuccess() {
                        // Отправить SDP через QR
                    }
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                })
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    fun setRemoteDescription(peerId: String, sdp: SessionDescription) {
        peerConnections[peerId]?.setRemoteDescription(sdp, object : SdpObserver {
            override fun onSetSuccess() {
                // Соединение установлено
            }
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        })
    }

    // Добавление ICE candidate и т.д.
}
