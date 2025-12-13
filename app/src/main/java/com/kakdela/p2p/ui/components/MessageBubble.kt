package com.kakdela.p2p.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kakdela.p2p.crypto.CryptoManager

@Composable
fun MessageBubble(message: String, isSent: Boolean) {
    // Расшифровка, если not sent
    val decrypted = if (!isSent) CryptoManager.decrypt(message.toByteArray(), byteArrayOf())?.toString(Charsets.UTF_8) ?: message else message
    Text(decrypted, Modifier.padding(8.dp))
}
