package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kakdela.p2p.p2p.TrustedPeersManager

@Composable
fun ContactsScreen(navController: NavHostController) {
    var showScanner by remember { mutableStateOf(false) }
    var showMyQr by remember { mutableStateOf(false) }
    var renamingPeer by remember { mutableStateOf<String?>(null) }

    val contacts = ContactsRepository.contacts.values.toList()

    Scaffold(
        topBar = { /* как было */ },
        floatingActionButton = { FloatingActionButton(onClick = { showScanner = true }) { Icon(Icons.Default.QrCodeScanner, "") } }
    ) { padding ->
        if (contacts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("Нет контактов\nДобавь по QR-коду")
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(contacts, key = { it.peerId }) { contact ->
                    ListItem(
                        headlineContent = { Text(contact.displayName) },
                        supportingContent = { Text(contact.peerId.takeLast(8)) },
                        trailingContent = {
                            IconButton(onClick = { renamingPeer = contact.peerId }) {
                                Icon(Icons.Default.Edit, "Переименовать")
                            }
                        },
                        modifier = Modifier.clickable {
                            navController.navigate("chat/${contact.peerId}")
                        }
                    )
                }
            }
        }
    }

    if (showScanner) {
        QrScannerScreen(
            onPeerAdded = { _, _ -> /* обновится автоматически */ },
            onDismiss = { showScanner = false }
        )
    }

    renamingPeer?.let { peerId ->
        val current = ContactsRepository.getById(peerId)
        var newName by remember { mutableStateOf(current?.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { renamingPeer = null },
            title = { Text("Переименовать") },
            text = { TextField(value = newName, onValueChange = { newName = it }) },
            confirmButton = {
                TextButton(onClick = {
                    ContactsRepository.rename(peerId, newName.trim())
                    renamingPeer = null
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { renamingPeer = null }) { Text("Отмена") } }
        )
    }
}
