// app/src/main/java/com/kakdela/p2p/ui/screens/ChatScreen.kt
package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kakdela.p2p.model.ChatMessage
import com.kakdela.p2p.model.ContactsRepository
import com.kakdela.p2p.webrtc.FileTransferManager
import com.kakdela.p2p.ui.components.VoiceMessageRecorder
import kotlinx.coroutines.launch

private val chatHistory = mutableMapOf<String, MutableList<ChatMessage>>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(peerId: String) {
    val contact = ContactsRepository.getById(peerId) ?: return
    val displayName = contact.displayName

    val messages = remember(peerId) {
        chatHistory.getOrPut(peerId) { mutableStateListOf() }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Фото/видео
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { FileTransferManager.sendFile(context, peerId, it) }
    }

    // Файлы
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { FileTransferManager.sendFile(context, peerId, it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(displayName, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            Column {
                // ГОЛОСОВЫЕ СООБЩЕНИЯ
                VoiceMessageRecorder(peerId = peerId) {
                    scope.launch { listState.animateScrollToItem(messages.lastIndex) }
                }
                
                // ТЕКСТ + ФАЙЛЫ
                InputBar(
                    peerId = peerId,
                    onPhotoPicked = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                    onFilePicked = { filePicker.launch("*/*") },
                    onMessageSent = {
                        scope.launch { listState.animateScrollToItem(messages.lastIndex) }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg, isFromMe = msg.isFromMe)
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
}

@Composable  // УБРАЛИ ДУБЛИРОВАНИЕ!
fun ChatBubble(message: ChatMessage, isFromMe: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                when {
                    message.isImage -> {
                        AsyncImage(
                            model = message.fileUri,
                            contentDescription = "Фото",
                            modifier = Modifier
                                .sizeIn(maxWidth = 260.dp, maxHeight = 360.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { /* открыть полноэкранно */ },
                            contentScale = ContentScale.Crop
                        )
                    }
                    message.isVideo -> {
                        Box(
                            modifier = Modifier
                                .sizeIn(maxWidth = 260.dp, maxHeight = 360.dp)
                                .background(Color.Black.copy(0.4f), RoundedCornerShape(16.dp))
                                .clickable { /* воспроизвести */ }
                        ) {
                            Icon(
                                Icons.Default.PlayCircleFilled,
                                contentDescription = "Видео",
                                tint = Color.White,
                                modifier = Modifier.size(72.dp).align(Alignment.Center)
                            )
                        }
                    }
                    message.isVoice -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Голосовое сообщение · ${message.duration ?: "0:12"}", color = Color.White)
                        }
                    }
                    message.isFile -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.InsertDriveFile, null, tint = Color.White.copy(0.9f))
                            Spacer(Modifier.width(8.dp))
                            Text(message.fileName ?: "Файл", color = Color.White)
                        }
                    }
                    else -> {
                        Text(
                            text = message.text ?: "",
                            color = if (isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = message.time,
                    color = (if (isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
