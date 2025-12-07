package com.kakdela.goterl

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.KeyPair

object CryptoCore {
    private val sodium = LazySodiumAndroid(SodiumAndroid())

    fun generateKeyPair(): KeyPair = sodium.cryptoBoxKeypair()

    fun encrypt(
        message: String,
        receiverPublicKey: ByteArray,
        senderSecretKey: ByteArray
    ): ByteArray {
        val nonce = sodium.randomBytesBuf(sodium.cryptoBoxNonceBytes())
        val encrypted = ByteArray(message.toByteArray().size + sodium.cryptoBoxMacBytes())

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
        if (encryptedData.size < sodium.cryptoBoxMacBytes() + sodium.cryptoBoxNonceBytes()) return null

        val nonce = encryptedData.copyOfRange(0, sodium.cryptoBoxNonceBytes())
        val ciphertext = encryptedData.copyOfRange(sodium.cryptoBoxNonceBytes(), encryptedData.size)

        val decrypted = ByteArray(ciphertext.size - sodium.cryptoBoxMacBytes())

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
