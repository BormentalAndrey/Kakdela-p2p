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

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        require(length % 2 == 0)
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    fun encryptMessage(text: String, theirPublicKeyHex: String): String {
        val msg = text.toByteArray()
        val theirKey = Key.fromHexString(theirPublicKeyHex).asBytes
        val nonce = sodium.randomBytesBuf(Box.NONCEBYTES)
        val cipher = ByteArray(msg.size + Box.MACBYTES)

        val ok = sodium.cryptoBoxEasy(
            cipher,
            msg,
            msg.size.toLong(),
            nonce,
            theirKey,
            getMyKeyPair().secretKey.asBytes
        )

        check(ok) { "Encryption failed" }

        return (nonce + cipher).toHex()
    }

    fun decryptMessage(encryptedHex: String, theirPublicKeyHex: String): String? {
        val data = encryptedHex.hexToBytes()
        if (data.size < Box.NONCEBYTES + Box.MACBYTES) return null

        val nonce = data.copyOfRange(0, Box.NONCEBYTES)
        val cipher = data.copyOfRange(Box.NONCEBYTES, data.size)
        val plain = ByteArray(cipher.size - Box.MACBYTES)
        val theirKey = Key.fromHexString(theirPublicKeyHex).asBytes

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
