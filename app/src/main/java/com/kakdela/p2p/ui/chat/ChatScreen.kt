package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kakdela.p2p.ui.chat.VoiceMessageButton
import com.kakdela.p2p.ui.theme.KakdelaTheme

// Заглушка для сообщений — потом заменишь на Room
data class Message(
    val text: String,
    val isFromMe: Boolean,
    val time: String = "12:34"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatId: String, navController: NavController) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            Message("Привет! Как дела?", false),
            Message("Всё отлично! А у тебя?", true),
            Message("Тоже супер", false),
            Message("Запустил свой P2P-мессенджер!", true),
        )
    }

    KakdelaTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Чат с $chatId", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Список сообщений
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message)
                    }
                }

                // Панель ввода
                InputBar(
                    text = text,
                    onTextChange = { text = it },
                    onSend = {
                        if (text.isNotBlank()) {
                            messages.add(Message(text, true))
                            text = ""
                        }
                    },
                    onAttach = {
                        // Открыть выбор файла
                        // context.startActivity(...)
                    }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (message.isFromMe)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = if (message.isFromMe) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = message.time,
                    color = if (message.isFromMe) Color.White.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit
) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Сообщение...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )

            IconButton(onClick = onAttach) {
                Icon(Icons.Default.AttachFile, contentDescription = "Файл")
            }

            VoiceMessageButton { file ->
                // TODO: отправить голосовое
            }

            IconButton(onClick = onSend) {
                Icon(Icons.Default.Send, contentDescription = "Отправить")
            }
        }
    }
}
