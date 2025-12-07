package com.kakdela.goterl

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.SodiumConstants
import com.goterl.lazysodium.utils.KeyPair

object CryptoCore {
    private val sodium = LazySodiumAndroid(SodiumAndroid())

    fun generateKeyPair(): KeyPair = sodium.cryptoBoxKeypair()

    fun encrypt(
        message: String,
        receiverPublicKey: ByteArray,
        senderSecretKey: ByteArray
    ): ByteArray {
        val nonce = sodium.randomBytesBuf(SodiumConstants.CRYPTO_BOX_NONCEBYTES)
        val encrypted = ByteArray(message.toByteArray().size + SodiumConstants.CRYPTO_BOX_MACBYTES)

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
        if (encryptedData.size < SodiumConstants.CRYPTO_BOX_MACBYTES + SodiumConstants.CRYPTO_BOX_NONCEBYTES) return null

        val nonce = encryptedData.copyOfRange(0, SodiumConstants.CRYPTO_BOX_NONCEBYTES)
        val ciphertext = encryptedData.copyOfRange(SodiumConstants.CRYPTO_BOX_NONCEBYTES, encryptedData.size)

        val decrypted = ByteArray(ciphertext.size - SodiumConstants.CRYPTO_BOX_MACBYTES)

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
