
package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kakdela.p2p.db.ChatMessageEntity
import com.kakdela.p2p.data.MessageRepository
import com.kakdela.p2p.trusted.TrustedPeersManager
import com.kakdela.p2p.ui.components.VoiceMessageRecorder
import com.kakdela.p2p.webrtc.FileTransferManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ChatScreen(peerId: String, onBack: () -> Unit) {
    val contact = TrustedPeersManager.getById(peerId) ?: return
    val displayName = contact.displayName
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var messages by remember { mutableStateOf(listOf<ChatMessageEntity>()) }

    LaunchedEffect(peerId) {
        MessageRepository.observeChat(peerId).collectLatest { newMessages ->
            messages = newMessages
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { FileTransferManager.sendFile(peerId, it, context) }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { FileTransferManager.sendFile(peerId, it, context) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(displayName) }) }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            LazyColumn(Modifier.weight(1f)) {
                items(messages) { message ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = if (message.isSent) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.isSent) Color(0xFF00FFF0) else Color(0xFF2A2A2A)
                            )
                        ) {
                            when (message.type) {
                                "text" -> Text(message.content, Modifier.padding(12.dp))
                                "image" -> AsyncImage(model = message.content, contentDescription = null, modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                "voice" -> Row {
                                    Icon(Icons.Default.PlayArrow, "Play")
                                    Text("${message.duration}s")
                                }
                            }
                        }
                    }
                }
            }
            InputBar(
                sendText = { text ->
                    coroutineScope.launch {
                        MessageRepository.insert(ChatMessageEntity(peerId = peerId, content = text, type = "text", isSent = true))
                    }
                    FileTransferManager.sendText(peerId, text)
                },
                sendFile = { filePicker.launch("*/*") },
                sendPhoto = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            VoiceMessageRecorder { voice ->
                coroutineScope.launch {
                    MessageRepository.insert(ChatMessageEntity(peerId = peerId, content = "voice_uri", type = "voice", isSent = true, duration = voice.size / 16000))
                }
                FileTransferManager.sendVoice(peerId, voice)
            }
        }
    }
}
