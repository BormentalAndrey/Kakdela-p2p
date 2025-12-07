package com.kakdela.goterl

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.KeyPair

object CryptoCore {
    private val sodium = LazySodiumAndroid(SodiumAndroid())

    private const val NONCE_BYTES = 24
    private const val MAC_BYTES = 16

    fun generateKeyPair(): KeyPair = sodium.cryptoBoxKeypair()

    fun encrypt(
        message: String,
        receiverPublicKey: ByteArray,
        senderSecretKey: ByteArray
    ): ByteArray {
        val nonce = sodium.randomBytesBuf(NONCE_BYTES)
        val encrypted = ByteArray(message.toByteArray().size + MAC_BYTES)

        sodium.cryptoBoxEasy(
            encrypted,
            message.toByteArray(),
            message.length.toLong(),
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
        if (encryptedData.size < MAC_BYTES + NONCE_BYTES) return null

        val nonce = encryptedData.copyOfRange(0, NONCE_BYTES)
        val ciphertext = encryptedData.copyOfRange(NONCE_BYTES, encryptedData.size)

        val decrypted = ByteArray(ciphertext.size - MAC_BYTES)

        val result = sodium.cryptoBoxOpenEasy(
            decrypted,
            ciphertext,
            ciphertext.size.toLong(),
            nonce,
            senderPublicKey,
            receiverSecretKey
        )

        return if (result) String(decrypted) else null
    }
}
