package com.kakdela.p2p.webrtc

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair

object CryptoManager {

    private val sodium = LazySodiumAndroid(SodiumAndroid())
    private var myKeyPair: KeyPair? = null

    fun getMyKeyPair(): KeyPair {
        if (myKeyPair == null) {
            myKeyPair = sodium.cryptoBoxKeypair()
        }
        return myKeyPair!!
    }

    // --- Helpers ---

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        require(length % 2 == 0) { "Invalid HEX length" }
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    // --- Encrypt ---

    fun encryptMessage(text: String, theirPublicKeyHex: String): String {
        val msg = text.toByteArray(Charsets.UTF_8)
        val theirKey = Key.fromHexString(theirPublicKeyHex).asBytes
        val nonce = sodium.randomBytesBuf(Box.NONCEBYTES)
        val cipherText = ByteArray(msg.size + Box.MACBYTES)

        val ok = sodium.cryptoBoxEasy(
            cipherText,
            msg,
            msg.size.toLong(),
            nonce,
            theirKey,
            getMyKeyPair().secretKey.asBytes
        )

        check(ok) { "Encryption failed" }

        return (nonce + cipherText).toHexString()
    }

    // --- Decrypt ---

    fun decryptMessage(encryptedHex: String, theirPublicKeyHex: String): String? {
        val full = encryptedHex.hexToBytes()
        if (full.size < Box.NONCEBYTES + Box.MACBYTES) return null

        val nonce = full.copyOfRange(0, Box.NONCEBYTES)
        val cipher = full.copyOfRange(Box.NONCEBYTES, full.size)
        val theirKey = Key.fromHexString(theirPublicKeyHex).asBytes
        val plain = ByteArray(cipher.size - Box.MACBYTES)

        val ok = sodium.cryptoBoxOpenEasy(
            plain,
            cipher,
            cipher.size.toLong(),
            nonce,
            theirKey,
            getMyKeyPair().secretKey.asBytes
        )

        return if (ok) plain.toString(Charsets.UTF_8) else null
    }
}
