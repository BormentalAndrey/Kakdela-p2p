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

    val trustedList = TrustedPeersManager.getAll()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Kakdela") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showScanner = true }) {
                Icon(Icons.Default.QrCodeScanner, "Добавить")
            }
        }
    ) { padding ->
        if (trustedList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("Нет контактов\nНажми кнопку ↓ и отсканируй QR друга")
            }
        } else {
            LazyColumn(contentPadding = padding + PaddingValues(16.dp)) {
                items(trustedList) { peerId ->
                    ListItem(
                        headlineContent = { Text(peerId) },
                        supportingContent = { Text("Нажми → чат") },
                        modifier = Modifier.clickable {
                            navController.navigate("chat/$peerId")
                        }
                    )
                    Divider()
                }
            }
        }
    }

    if (showScanner) {
        QrScannerScreen(
            onPeerAdded = { /* можно показать тост */ },
            onDismiss = { showScanner = false }
        )
    }

    if (showMyQr) MyQrScreen()
    // можно добавить кнопку "Показать мой QR" в меню
}
