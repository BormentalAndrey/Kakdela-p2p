package com.kakdela.p2p.crypto

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
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

    fun encrypt(data: ByteArray, theirPublicKey: ByteArray): ByteArray {
        val kp = getMyKeyPair()
        val nonce = sodium.randomBytesBuf(24)
        val encrypted = ByteArray(data.size + sodium.cryptoBoxMacBytes())
        sodium.cryptoBoxEasy(
            encrypted, data, data.size.toLong(), nonce,
            theirPublicKey, kp.secretKey.asBytes
        )
        return nonce + encrypted
    }

    fun decrypt(encrypted: ByteArray, theirPublicKey: ByteArray): ByteArray? {
        if (encrypted.size < 24) return null
        val nonce = encrypted.copyOfRange(0, 24)
        val ciphertext = encrypted.copyOfRange(24, encrypted.size)
        val kp = getMyKeyPair()
        val decrypted = ByteArray(ciphertext.size - sodium.cryptoBoxMacBytes())
        val result = sodium.cryptoBoxOpenEasy(
            decrypted, ciphertext, ciphertext.size.toLong(), nonce,
            theirPublicKey, kp.secretKey.asBytes
        )
        return if (result) decrypted else null
    }
}
