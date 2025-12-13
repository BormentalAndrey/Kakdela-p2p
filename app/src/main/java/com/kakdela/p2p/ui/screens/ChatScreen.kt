package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kakdela.p2p.model.ChatMessage
import com.kakdela.p2p.trusted.TrustedPeersManager
import com.kakdela.p2p.ui.components.VoiceMessageRecorder
import com.kakdela.p2p.webrtc.FileTransferManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

@Composable
fun ChatScreen(peerId: String, onBack: () -> Unit) {
    val contact = TrustedPeersManager.getById(peerId) ?: return
    val displayName = contact.displayName
    val context = LocalContext.current
    val messages = remember { mutableStateListOf<ChatMessage>() } // Load from Room in real

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { FileTransferManager.sendFile(peerId, it, context) }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { FileTransferManager.sendFile(peerId, it, context) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(displayName) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
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
                                "text" -> Text(message.content, modifier = Modifier.padding(12.dp))
                                "image" -> AsyncImage(
                                    model = message.content,
                                    contentDescription = "Image",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                "voice" -> Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, "Play")
                                    Text("${message.duration ?: 0}s")
                                }
                            }
                        }
                    }
                }
            }
            InputBar(
                onSendText = { text ->
                    FileTransferManager.sendText(peerId, text)
                    messages.add(ChatMessage(content = text, type = "text", isSent = true))
                },
                onSendPhoto = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onSendFile = { filePicker.launch("*/*") }
            )
            VoiceMessageRecorder(onVoiceSent = { voice ->
                FileTransferManager.sendVoice(peerId, voice)
                messages.add(ChatMessage(content = "voice", type = "voice", isSent = true, duration = voice.size / 16000)) // Approximate
            })
        }
    }
}
