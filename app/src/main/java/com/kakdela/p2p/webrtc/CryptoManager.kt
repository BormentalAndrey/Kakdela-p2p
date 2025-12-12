package com.kakdela.p2p.webrtc

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.utils.KeyPair

class CryptoManager(private val sodium: LazySodiumAndroid) {

    private val keyPair: KeyPair = sodium.cryptoBoxKeypair()

    val publicKeyHex: String get() = sodium.toHex(keyPair.publicKey.asBytes)
    val privateKey get() = keyPair.secretKey.asBytes

    fun encrypt(message: ByteArray, theirPublicKeyHex: String): ByteArray {
        val theirPub = sodium.sodiumHex2bin(theirPublicKeyHex)
        return sodium.cryptoBoxSealEasy(message, theirPub)
    }

    fun decrypt(cipher: ByteArray): ByteArray {
        return sodium.cryptoBoxSealOpenEasy(cipher, keyPair)
    }
}
