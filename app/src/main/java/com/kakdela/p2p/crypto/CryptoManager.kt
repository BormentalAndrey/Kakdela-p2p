package com.kakdela.p2p.crypto

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.KeyPair

class CryptoManager {
    private val lazySodium = LazySodiumAndroid(SodiumAndroid())

    fun generateKeyPair(): KeyPair {
        return lazySodium.cryptoBoxKeypair()
    }

    fun encrypt(message: ByteArray, receiverPublicKey: ByteArray, senderPrivateKey: ByteArray): ByteArray? {
        return lazySodium.cryptoBoxEasy(message, receiverPublicKey, senderPrivateKey)
    }

    fun decrypt(ciphertext: ByteArray, senderPublicKey: ByteArray, receiverPrivateKey: ByteArray): ByteArray? {
        return lazySodium.cryptoBoxOpenEasy(ciphertext, senderPublicKey, receiverPrivateKey)
    }
}
