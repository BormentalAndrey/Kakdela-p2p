package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Photo

@Composable
fun InputBar(onSendText: (String) -> Unit, onSendPhoto: () -> Unit, onSendFile: () -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AttachFile,
            contentDescription = "Attach File",
            modifier = Modifier.clickable(onClick = onSendFile)
        )
        Icon(
            Icons.Default.Photo,
            contentDescription = "Send Photo",
            modifier = Modifier.clickable(onClick = onSendPhoto)
        )
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Сообщение") }
        )
        if (text.isNotBlank()) {
            IconButton(onClick = { onSendText(text); text = "" }) {
                Icon(Icons.Default.Send, "Send")
            }
        }
    }
}
