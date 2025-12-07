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
        val nonce = sodium.randomBytesBuf(sodium.cryptoBoxNonceBytes()) // используем метод библиотеки
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
        val nonceBytes = sodium.cryptoBoxNonceBytes()
        val macBytes = sodium.cryptoBoxMacBytes()

        if (encryptedData.size < macBytes + nonceBytes) return null

        val nonce = encryptedData.copyOfRange(0, nonceBytes)
        val ciphertext = encryptedData.copyOfRange(nonceBytes, encryptedData.size)

        val decrypted = ByteArray(ciphertext.size - macBytes)

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
