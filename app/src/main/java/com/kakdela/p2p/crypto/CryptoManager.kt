// app/src/main/java/com/kakdela/p2p/crypto/CryptoManager.kt
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

    /** Шифруем сообщение для собеседника */
    fun encrypt(plainText: String, theirPublicKeyHex: String): String {
        val messageBytes = plainText.toByteArray(Charsets.UTF_8)
        val theirPublicKey = Key.fromHexString(theirPublicKeyHex)

        val nonce = sodium.randomBytesBuf(Box.NONCEBYTES)
        val cipher = ByteArray(messageBytes.size + Box.MACBYTES)

        val success = sodium.cryptoBoxEasy(
            cipher,
            messageBytes,
            messageBytes.size.toLong(),
            nonce,
            theirPublicKey.asBytes,
            getMyKeyPair().secretKey.asBytes
        )

        check(success) { "Шифрование не удалось" }

        // nonce (24 байта) + шифротекст с MAC
        return (nonce + cipher).toHexString()
    }

    /** Расшифровываем входящее сообщение */
    fun decrypt(encryptedHex: String, theirPublicKeyHex: String): String? {
        val encryptedBytes = encryptedHex.hexToByteArray()
        if (encryptedBytes.size < Box.NONCEBYTES + Box.MACBYTES) return null

        val nonce = encryptedBytes.copyOfRange(0, Box.NONCEBYTES)
        val cipherText = encryptedBytes.copyOfRange(Box.NONCEBYTES, encryptedBytes.size)

        val theirPublicKey = Key.fromHexString(theirPublicKeyHex)

        val plain = ByteArray(cipherText.size - Box.MACBYTES)

        val success = sodium.cryptoBoxOpenEasy(
            plain,
            cipherText,
            cipherText.size.toLong(),
            nonce,
            theirPublicKey.asBytes,
            getMyKeyPair().secretKey.asBytes
        )

        return if (success) plain.toString(Charsets.UTF_8) else null
    }
}

// Вспомогательные расширения
private fun ByteArray.toHexString(): String =
    joinToString("") { "%02x".format(it.toInt() and 0xFF) }

private fun String.hexToByteArray(): ByteArray {
    check(length % 2 == 0) { "Нечётная длина hex-строки" }
    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
