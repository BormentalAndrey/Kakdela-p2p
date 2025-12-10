package com.kakdela.p2p.crypto

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.KeyPair

object CryptoManager {

    private val lazySodium = LazySodiumAndroid(SodiumAndroid())

    private var myKeyPair: KeyPair? = null

    fun getMyKeyPair(): KeyPair {
        if (myKeyPair == null) {
            myKeyPair = lazySodium.cryptoBoxKeypair()
        }
        return myKeyPair!!
    }

    fun encrypt(data: ByteArray, theirPublicKey: ByteArray): ByteArray {
        val kp = getMyKeyPair()
        val nonce = lazySodium.randomBytesBuf(Box.NONCEBYTES)
        val ciphertextWithMac = ByteArray(data.size + Box.MACBYTES)

        val success = lazySodium.cryptoBoxEasy(
            ciphertextWithMac,
            data,
            data.size.toLong(),
            nonce,
            theirPublicKey,
            kp.secretKey.asBytes
        )

        check(success) { "Encryption failed" }

        return nonce + ciphertextWithMac
    }

    fun decrypt(encryptedWithNonce: ByteArray, theirPublicKey: ByteArray): ByteArray? {
        if (encryptedWithNonce.size < Box.NONCEBYTES + Box.MACBYTES) return null

        val nonce = encryptedWithNonce.copyOfRange(0, Box.NONCEBYTES)
        val ciphertextWithMac = encryptedWithNonce.copyOfRange(Box.NONCEBYTES, encryptedWithNonce.size)

        val kp = getMyKeyPair()
        val plaintext = ByteArray(ciphertextWithMac.size - Box.MACBYTES)

        val success = lazySodium.cryptoBoxOpenEasy(
            plaintext,
            ciphertextWithMac,
            ciphertextWithMac.size.toLong(),
            nonce,
            theirPublicKey,
            kp.secretKey.asBytes   // ← ВОТ ТУТ БЫЛО "as" → ИСПРАВЛЕНО НА "asBytes"
        )

        return if (success) plaintext else null
    }
}
