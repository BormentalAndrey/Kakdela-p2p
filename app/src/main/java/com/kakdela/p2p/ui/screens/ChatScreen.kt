package com.kakdela.p2p.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.kakdela.p2p.data.MessageRepository

@Composable
fun ChatListScreen(peerId: String) {
    val messages = MessageRepository.observeChat(peerId).collectAsState(initial = emptyList())
    LazyColumn {
        items(messages.value) { message ->
            Text(message.content)
        }
    }
}
