// app/src/main/java/com/kakdela/p2p/webrtc/WebRtcManager.kt
object WebRtcManager {
    private val peerConnections = mutableMapOf<String, PeerConnection>()

    fun startDirectConnection(peerId: String, theirPublicKeyHex: String, iceServers: String) {
        // Создаём WebRTC соединение напрямую
        // Используем бесплатные Google STUN
        // После подключения — открывается DataChannel
        // По нему шлём зашифрованные сообщения через CryptoManager
    }

    fun sendMessage(peerId: String, text: String) {
        val encrypted = CryptoManager.encrypt(text, theirPublicKeyHex)
        peerConnections[peerId]?.dataChannel?.send(encrypted)
    }
}
