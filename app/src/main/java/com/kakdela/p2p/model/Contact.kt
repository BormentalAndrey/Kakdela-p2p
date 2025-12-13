package com.kakdela.p2p.model

data class Contact(
    val peerId: String,
    val displayName: String,
    val publicKeyHex: String = "",
    val iceServers: List<String> = emptyList()
)
