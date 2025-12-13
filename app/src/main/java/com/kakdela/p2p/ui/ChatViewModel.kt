
package com.kakdela.p2p.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakdela.p2p.data.MessageRepository
import com.kakdela.p2p.db.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ChatViewModel(private val peerId: String) : ViewModel() {
    val messages: Flow<List<ChatMessageEntity>> = MessageRepository.observeChat(peerId)

    fun sendMessage(text: String) {
        viewModelScope.launch {
            MessageRepository.insert(ChatMessageEntity(peerId = peerId, content = text, type = "text", isSent = true))
        }
    }
}
