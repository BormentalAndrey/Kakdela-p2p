
package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.kakdela.p2p.trusted.TrustedPeersManager
import com.kakdela.p2p.webrtc.WebRtcManager
import com.kakdela.p2p.model.Contact

@Composable
fun QrScannerScreen(onDismiss: () -> Unit) {
    var showNameDialog by remember { mutableStateOf<String?>(null) }
    var pendingPeerId by remember { mutableStateOf<String?>(null) }
    var pendingPublicKeyHex by remember { mutableStateOf<String?>(null) }
    var pendingIceServers by remember { mutableStateOf<List<String>?>(null) }

    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val data = result.contents ?: return@rememberLauncherForActivityResult

        // Парсинг QR: kakdela://connect?id=PEER_ID&pubkey=PUB_KEY_HEX
        val params = data.split("?").getOrNull(1)?.split("&") ?: return@rememberLauncherForActivityResult
        val peerId = params.find { it.startsWith("id=") }?.substringAfter("=") ?: return@rememberLauncherForActivityResult
        val publicKeyHex = params.find { it.startsWith("pubkey=") }?.substringAfter("=")
        val iceServers = params.find { it.startsWith("iceServers=") }?.substringAfter("=")?.split(",") ?: listOf("stun:stun.l.google.com:19302")

        pendingPeerId = peerId
        pendingPublicKeyHex = publicKeyHex
        pendingIceServers = iceServers
        showNameDialog = peerId
    }

    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Button(onClick = { launcher.launch(ScanOptions()) }) {
            Text("Сканировать QR")
        }
    }

    showNameDialog?.let { peerId ->
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNameDialog = null },
            title = { Text("Имя контакта") },
            text = { TextField(value = name, onValueChange = { name = it }) },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        TrustedPeersManager.addPeer(Contact(peerId, name.trim(), pendingPublicKeyHex ?: "", pendingIceServers ?: emptyList()))
                        WebRtcManager.initiateConnection(peerId, pendingPublicKeyHex ?: "", pendingIceServers ?: emptyList())
                    }
                    showNameDialog = null
                    onDismiss()
                }) { Text("Добавить") }
            }
        )
    }
}
