package com.kakdela.p2p.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.kakdela.p2p.db.AppDatabase
import com.kakdela.p2p.db.ChatMessage
import com.kakdela.p2p.data.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "kakdela-db").build()
    private val repo = MessageRepository(db.messageDao())

    val messages = repo.messagesForChat("default").map { it }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()) as StateFlow<List<ChatMessage>>

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val msg = ChatMessage(chatId = "default", senderId = "me", body = text ?: "", timestamp = System.currentTimeMillis())
            repo.sendMessage(msg)
        }
    }
}
