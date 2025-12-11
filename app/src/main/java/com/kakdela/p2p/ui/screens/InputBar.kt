// app/src/main/java/com/kakdela/p2p/ui/screens/InputBar.kt
package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.kakdela.p2p.webrtc.FileTransferManager
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputBar(
    peerId: String,
    onMessageSent: () -> Unit = {}
) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Галерея (фото/видео)
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { FileTransferManager.sendFile(context, peerId, it) }
    }

    // Любые файлы
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { FileTransferManager.sendFile(context, peerId, it) }
    }

    // Голосовое сообщение
    val voiceRecorder = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Будет реализовано позже — пока заглушка
    }

    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка прикрепить
            IconButton(onClick = {
                // Покажем меню
                // (в реальный код меню будет ниже)
            }) {
                Icon(Icons.Default.AttachFile, "Прикрепить", tint = MaterialTheme.colorScheme.primary)
            }

            // Поле ввода
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                placeholder = { Text("Сообщение...") },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.width(8.dp))

            // Кнопка отправки
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        FileTransferManager.sendText(peerId, text)
                        text = ""
                        onMessageSent()
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Отправить",
                    tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }

    // Меню прикрепления (всплывающее)
    var showAttachMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { showAttachMenu = true }) {
        Icon(Icons.Default.AttachFile, "Прикрепить")
    }

    if (showAttachMenu) {
        AlertDialog(
            onDismissRequest = { showAttachMenu = false },
            title = { Text("Прикрепить") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Фото или видео") },
                        leadingContent = { Icon(Icons.Default.Photo, "") },
                        modifier = Modifier.clickable {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                            showAttachMenu = false
                        }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Файл") },
                        leadingContent = { Icon(Icons.Default.InsertDriveFile, "") },
                        modifier = Modifier.clickable {
                            filePicker.launch("*/*")
                            showAttachMenu = false
                        }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Голосовое сообщение") },
                        leadingContent = { Icon(Icons.Default.Mic, "") },
                        modifier = Modifier.clickable {
                            // Пока заглушка — скоро добавим запись
                            showAttachMenu = false
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachMenu = false }) { Text("Отмена") }
            }
        )
    }
}
