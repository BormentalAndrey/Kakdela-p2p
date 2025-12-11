// app/src/main/java/com/kakdela/p2p/ui/screens/ChatScreen.kt
package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakdela.p2p.ui.model.Message
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val chatHistory = mutableMapOf<String, MutableList<Message>>() // peerId → список сообщений

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(peerId: String) {
    val displayName = peerId.replace("KAKDELA_", "").take(12)
    val messages = remember(peerId) { chatHistory.getOrPut(peerId) { mutableStateListOf() } }
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(displayName) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            InputBar(
                text = text,
                onTextChange = { text = it },
                onSend = {
                    if (text.isNotBlank()) {
                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        messages.add(Message(text, true, time))
                        text = ""
                        scope.launch {
                            listState.animateScrollToItem(messages.lastIndex)
                        }
                    }
                }
            )
        }
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message)
                }
            }
        }
    }

    // Автоскролл при появлении нового сообщения
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
}

// Остальные функции MessageBubble и InputBar — оставляем как у тебя (они идеальны)
@Composable
fun MessageBubble(message: Message) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (message.isFromMe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    color = if (message.isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.time,
                    color = (if (message.isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        .copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f).height(56.dp),
                placeholder = { Text("Сообщение...") },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialScheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = onSend, enabled = text.isNotBlank()) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Отправить",
                    tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}
