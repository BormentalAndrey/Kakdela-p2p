package com.kakdela.p2p.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kakdela.p2p.db.AppDatabase
import com.kakdela.p2p.db.ChatMessage
import com.kakdela.p2p.data.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ChatViewModel — интегрирован с Room через AppDatabase и MessageRepository.
 * Совместим с репозиторием и DAO из PDF (chatMessageDao / ChatMessageDao).
 *
 * В UI используем: val vm: ChatViewModel = viewModel() и vm.messages.collectAsState()
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    // Берём инстанс базы (реализация AppDatabase — в проекте, см. PDF).
    private val db = AppDatabase.getInstance(application)
    // MessageRepository в PDF ожидает ChatMessageDao в конструкторе.
    private val repo = MessageRepository(db.chatMessageDao())

    // Наблюдаемые сообщения для чата "default" (можно заменить на реальный chatId)
    // В PDF в некоторых местах использовался messagesForChat(), в репозитории — observeChat().
    // Здесь мы используем observeChat и приводим к StateFlow.
    val messages: StateFlow<List<ChatMessage>> =
        repo.observeChat("default")
            .map { it } // passthrough — даём возможность добавить трансформации позже
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // В PDF репозиторий имел sendMessage(chatId, author, text)
            repo.sendMessage(chatId = "default", author = "me", text = text)
        }
    }
}
