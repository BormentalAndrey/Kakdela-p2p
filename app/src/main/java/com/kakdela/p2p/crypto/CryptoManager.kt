package com.kakdela.p2p.crypto

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair

object CryptoManager {

    private val sodium = LazySodiumAndroid(SodiumAndroid())

    private var myKeyPair: KeyPair? = null

    /** Генерируем или возвращаем свою постоянную пару ключей */
    fun getMyKeyPair(): KeyPair {
        if (myKeyPair == null) {
            myKeyPair = sodium.cryptoBoxKeypair()
        }
        return myKeyPair!!
    }

    /** Шифруем сообщение для получателя (end-to-end) */
    fun encrypt(message: ByteArray, receiverPublicKey: ByteArray): ByteArray? {
        val publicKey = Key.fromBytes(receiverPublicKey)
        val privateKey = Key.fromBytes(getMyKeyPair().secretKey.asBytes)
        return sodium.cryptoBoxEasy(message, publicKey.asBytes, privateKey.asBytes)
    }

    /** Расшифровываем сообщение от отправителя */
    fun decrypt(ciphertext: ByteArray, senderPublicKey: ByteArray): ByteArray? {
        val publicKey = Key.fromBytes(senderPublicKey)
        val privateKey = Key.fromBytes(getMyKeyPair().secretKey.asBytes)
        return sodium.cryptoBoxOpenEasy(ciphertext, publicKey.asBytes, privateKey.asBytes)
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    private fun String.hexToByteArray(): ByteArray {
        check(length % 2 == 0) { "Нечётная длина hex-строки" }
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
