package com.kakdela.p2p.trusted

class TrustedPeersManager {
    private val peers = mutableSetOf<String>()

    fun add(id: String) = peers.add(id)
    fun all(): Set<String> = peers
}
