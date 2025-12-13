package com.kakdela.p2p.crypto

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.interfaces.Random

object CryptoManager {

    private val lazySodium = LazySodiumAndroid(SodiumAndroid())

    fun generateKeyPair(): KeyPair {
        val publicKey = ByteArray(Box.PUBLICKEYBYTES)
        val secretKey = ByteArray(Box.SECRETKEYBYTES)
        lazySodium.cryptoBoxKeypair(publicKey, secretKey)
        return KeyPair(Key.fromBytes(publicKey), Key.fromBytes(secretKey))
    }

    fun encrypt(message: ByteArray, nonce: ByteArray, receiverPublicKey: Key, senderPrivateKey: Key): ByteArray {
        val cipher = ByteArray(message.size + Box.MACBYTES)
        lazySodium.cryptoBoxEasy(cipher, message, message.size.toLong(), nonce, receiverPublicKey.asBytes, senderPrivateKey.asBytes)
        return cipher
    }

    fun decrypt(cipher: ByteArray, nonce: ByteArray, senderPublicKey: Key, receiverPrivateKey: Key): ByteArray? {
        val message = ByteArray(cipher.size - Box.MACBYTES)
        val success = lazySodium.cryptoBoxOpenEasy(message, cipher, cipher.size.toLong(), nonce, senderPublicKey.asBytes, receiverPrivateKey.asBytes)
        return if (success) message else null
    }

    fun generateNonce(): ByteArray {
        val nonce = ByteArray(Box.NONCEBYTES)
        lazySodium.randomBytesBuf(nonce, Box.NONCEBYTES)
        return nonce
    }

    val myKeyPair: KeyPair by lazy { generateKeyPair() }
}
