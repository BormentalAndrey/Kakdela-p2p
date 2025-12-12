package com.kakdela.p2p.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kakdela.p2p.db.AppDatabase
import com.kakdela.p2p.data.MessageRepository
import com.kakdela.p2p.db.ChatMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MessageRepository(
        AppDatabase.getInstance(app).chatMessageDao()
    )

    val messages: StateFlow<List<ChatMessage>> =
        repo.observeChat("default")
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repo.sendMessage("default", "me", text)
        }
    }
}
