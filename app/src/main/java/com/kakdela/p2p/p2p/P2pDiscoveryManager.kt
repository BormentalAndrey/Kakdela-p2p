object P2pDiscoveryManager {
    // … твой текущий код …

    // ←←←← НОВАЯ ПЕРЕМЕННАЯ
    private val trustedPeers = mutableSetOf<String>()

    fun addTrustedPeer(peerId: String) {
        trustedPeers.add(peerId)
    }

    fun isPeerTrusted(peerId: String): Boolean {
        return trustedPeers.contains(peerId)
    }
}
