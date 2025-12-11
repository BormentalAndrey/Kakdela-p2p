// app/src/main/java/com/kakdela/p2p/ui/screens/QrScannerScreen.kt
package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.kakdela.p2p.model.ContactsRepository
import com.kakdela.p2p.webrtc.WebRtcManager

@Composable
fun QrScannerScreen(
    onDismiss: () -> Unit
) {
    var showNameDialog by remember { mutableStateOf<String?>(null) }
    var pendingPeerData by remember { mutableStateOf<PeerData?>(null) }

    val uriHandler = LocalUriHandler.current
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val data = result.contents ?: return@rememberLauncherForActivityResult

        // Поддерживаем все форматы QR от Kakdela
        when {
            data.startsWith("kakdela://connect?") || data.startsWith("https://kakdela.app/add/") -> {
                val url = if (data.startsWith("http")) data else data.replace("kakdela://connect?", "https://kakdela.app/add/?")
                val uri = android.net.Uri.parse(url)

                val peerId = uri.getQueryParameter("id") ?: return@rememberLauncherForActivityResult
                val publicKey = uri.getQueryParameter("pk") ?: return@rememberLauncherForActivityResult
                val ice = uri.getQueryParameter("ice") ?: "stun:stun.l.google.com:19302"

                pendingPeerData = PeerData(peerId, publicKey, ice)
                showNameDialog = peerId
            }

            data.startsWith("kakdela://peer/") -> {
                val peerId = data.removePrefix("kakdela://peer/")
                pendingPeerData = PeerData(peerId, null, null)
                showNameDialog = peerId
            }

            else -> {
                // Попытка открыть как обычную ссылку (на случай https://kakdela.app/add/...)
                try { uriHandler.openUri(data) } catch (e: Exception) {}
            }
        }

        if (showNameDialog == null) onDismiss()
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Наведи камеру на QR-код Kakdela")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
        )
    }

    // Диалог ввода имени
    showNameDialog?.let { peerId ->
        var name by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNameDialog = null; onDismiss() },
            title = { Text("Как зовут контакт?") },
            text = {
                Column {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Введите имя") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "ID: ${peerId.takeLast(12)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            val data = pendingPeerData!!
                            ContactsRepository.addOrUpdate(
                                peerId = data.peerId,
                                displayName = name.trim(),
                                publicKeyHex = data.publicKeyHex,
                                iceServers = data.iceServers
                            )

                            // Если есть ключ и ICE — сразу пробуем соединиться через интернет!
                            if (data.publicKeyHex != null && data.iceServers != null) {
                                WebRtcManager.initiateConnection(data.peerId, data.publicKeyHex, data.iceServers)
                            }
                        }
                        showNameDialog = null
                        onDismiss()
                    }
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = null; onDismiss() }) { Text("Отмена") }
            }
        )
    }

    // Экран сканера
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Сканирование QR-кода…", color = Color.White)
    }
}

// Временная структура для передачи данных из QR
private data class PeerData(
    val peerId: String,
    val publicKeyHex: String?,
    val iceServers: String?
)
