package com.kakdela.goterl

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair

object CryptoCore {
    private val sodium = LazySodiumAndroid(SodiumAndroid())

    fun generateKeyPair(): KeyPair = sodium.cryptoBoxKeypair()

    fun encrypt(
        message: String,
        receiverPublicKey: ByteArray,
        senderSecretKey: ByteArray
    ): ByteArray {
        val nonce = sodium.randomBytesBuf(24)
        val encrypted = ByteArray(message.toByteArray().size + 40)
        sodium.cryptoBoxEasy(
            encrypted,
            message.toByteArray(),
            encrypted.size.toLong(),
            nonce,
            receiverPublicKey,
            senderSecretKey
        )
        return nonce + encrypted
    }

    fun decrypt(
        encryptedData: ByteArray,
        senderPublicKey: ByteArray,
        receiverSecretKey: ByteArray
    ): String? {
        if (encryptedData.size < 40) return null
        val nonce = encryptedData.copyOfRange(0, 24)
        val cipher = encryptedData.copyOfRange(24, encryptedData.size)
        val decrypted = ByteArray(cipher.size - 40)
        val result = sodium.cryptoBoxOpenEasy(
            decrypted,
            cipher,
            cipher.size.toLong(),
            nonce,
            senderPublicKey,
            receiverSecretKey
        )
        return if (result) String(decrypted) else null
    }
}
