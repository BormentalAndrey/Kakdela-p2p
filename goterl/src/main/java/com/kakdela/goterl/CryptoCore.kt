package com.kakdela.goterl

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.KeyPair

object CryptoCore {
    private val sodium = LazySodiumAndroid(SodiumAndroid())

    fun generateKeyPair(): KeyPair = sodium.cryptoBoxKeypair()

    fun encrypt(message: String, theirPublicKey: ByteArray, mySecretKey: ByteArray): ByteArray {
        val nonce = sodium.randomBytesBuf(24)
        val encrypted = ByteArray(message.length + sodium.cryptoBoxMacBytes())
        sodium.cryptoBoxEasy(
            encrypted, message.toByteArray(), message.length.toLong(),
            nonce, theirPublicKey, mySecretKey
        )
        return nonce + encrypted
    }

    fun decrypt(encrypted: ByteArray, theirPublicKey: ByteArray, mySecretKey: ByteArray): String? {
        if (encrypted.size < 40) return null
        val nonce = encrypted.copyOf(24)
        val cipher = encrypted.copyOfRange(24, encrypted.size)
        val decrypted = ByteArray(cipher.size - sodium.cryptoBoxMacBytes())
        val result = sodium.cryptoBoxOpenEasy(
            decrypted, cipher, cipher.size.toLong(), nonce, theirPublicKey, mySecretKey
        )
        return if (result) String(decrypted) else null
    }
}
