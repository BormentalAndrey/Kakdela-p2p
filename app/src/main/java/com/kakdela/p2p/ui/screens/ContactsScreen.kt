package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kakdela.p2p.trusted.TrustedPeersManager

@Composable
fun ContactsScreen(onOpenChat: (String) -> Unit) {
    val contacts = TrustedPeersManager.getAll()
    var showScanner by remember { mutableStateOf(false) }
    var renamingPeer by remember { mutableStateOf<String?>(null) }
    val p2pService = P2PService(LocalContext.current)

    LaunchedEffect(Unit) {
        p2pService.discoverPeers(onSuccess = { /* update UI */ }, onFailure = { /* error */ })
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Контакты") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showScanner = true }) { Icon(Icons.Default.QrCodeScanner, "") } }
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("Нет контактов\nДобавь по QR-коду")
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(contacts) { contact ->
                    ListItem(
                        headlineContent = { Text(contact.displayName) },
                        supportingContent = { Text(contact.peerId.takeLast(8)) },
                        trailingContent = {
                            IconButton(onClick = { renamingPeer = contact.peerId }) {
                                Icon(Icons.Default.Edit, "Переименовать")
                            }
                        },
                        modifier = Modifier.clickable {
                            onOpenChat(contact.peerId)
                        }
                    )
                }
            }
        }
    }

    if (showScanner) {
        QrScannerScreen(onDismiss = { showScanner = false })
    }

    renamingPeer?.let { peerId ->
        var newName by remember { mutableStateOf(TrustedPeersManager.getById(peerId)?.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { renamingPeer = null },
            title = { Text("Переименовать") },
            text = { TextField(value = newName, onValueChange = { newName = it }) },
            confirmButton = {
                TextButton(onClick = {
                    TrustedPeersManager.rename(peerId, newName.trim())
                    renamingPeer = null
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { renamingPeer = null }) { Text("Отмена") } }
        )
    }
}
