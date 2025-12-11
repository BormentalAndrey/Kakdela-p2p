package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QrScannerScreen(
    onPeerAdded: (String, String) -> Unit,  // теперь имя тоже передаём
    onDismiss: () -> Unit
) {
    var showNameDialog by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { data ->
            if (data.startsWith("kakdela://peer/")) {
                val peerId = data.removePrefix("kakdela://peer/")
                showNameDialog = peerId
            }
        }
        if (showNameDialog == null) onDismiss()
    }

    LaunchedEffect(Unit) { launcher.launch(ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setPrompt("Наведи на QR-код Kakdela")
        setBeepEnabled(true)
    })}

    showNameDialog?.let { peerId ->
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { onDismiss(); showNameDialog = null },
            title = { Text("Как зовут контакт?") },
            text = {
                TextField(value = name, onValueChange = { name = it }, placeholder = { Text("Имя") })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        ContactsRepository.addOrUpdate(peerId, name.trim())
                        onPeerAdded(peerId, name.trim())
                    }
                    showNameDialog = null
                    onDismiss()
                }) { Text("Добавить") }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = null; onDismiss() }) { Text("Отмена") } }
        )
    }
}
