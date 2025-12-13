package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kakdela.p2p.model.Contact
import com.kakdela.p2p.trusted.TrustedPeersManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ContactsScreen(onOpenChat: (String) -> Unit) {
    val contacts = TrustedPeersManager.getAll()
    var showScanner by remember { mutableStateOf(false) }
    var renamingPeer by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Контакты") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showScanner = true }) { Icon(Icons.Default.QrCode, "Scan") } }
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет контактов. Добавьте по QR.")
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(contacts) { contact ->
                    ListItem(
                        headlineContent = { Text(contact.displayName) },
                        supportingContent = { Text(contact.peerId.takeLast(8)) },
                        trailingContent = {
                            IconButton(onClick = { renamingPeer = contact.peerId }) {
                                Icon(Icons.Default.Edit, "Rename")
                            }
                        },
                        modifier = Modifier.clickable { onOpenChat(contact.peerId) }
                    )
                }
            }
        }
    }

    if (showScanner) {
        QrScannerScreen(onPeerAdded = { peerId, publicKeyHex, iceServers ->
            TrustedPeersManager.addPeer(Contact(peerId, "New Contact", publicKeyHex))
            showScanner = false
        })
    }

    renamingPeer?.let { peerId ->
        val current = TrustedPeersManager.getById(peerId)
        var newName by remember { mutableStateOf(current?.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { renamingPeer = null },
            title = { Text("Переименовать") },
            text = { TextField(value = newName, onValueChange = { newName = it }) },
            confirmButton = {
                TextButton(onClick = {
                    TrustedPeersManager.rename(peerId, newName.trim())
                    renamingPeer = null
                }) { Text("Сохранить") }
            }
        )
    }
}
