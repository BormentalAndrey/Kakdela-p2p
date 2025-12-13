package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import java.util.UUID

@Composable
fun InputBar(
    sendText: (String) -> Unit,
    sendFile: () -> Unit,
    sendPhoto: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AttachFile,
            "File",
            modifier = Modifier.clickable { sendFile() }
        )
        Icon(
            Icons.Default.Photo,
            "Photo",
            modifier = Modifier.clickable { sendPhoto() }
        )
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Сообщение") }
        )
        if (text.isNotBlank()) {
            IconButton(onClick = { sendText(text); text = "" }) {
                Icon(Icons.Default.Send, "Send")
            }
        }
    }
}
