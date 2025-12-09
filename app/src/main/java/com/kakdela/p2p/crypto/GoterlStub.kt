package com.kakdela.p2p.crypto

// Temporary stub for goterl imports in CryptoManager.kt
// Replace with real impl in Step 3 (WebRTC signaling)
object Goterl {
    // Dummy functions to satisfy compiler
    fun init() {}
    fun generateKey() = ByteArray(32)
    fun encrypt(data: ByteArray, key: ByteArray) = data
    fun decrypt(data: ByteArray, key: ByteArray) = data
}
