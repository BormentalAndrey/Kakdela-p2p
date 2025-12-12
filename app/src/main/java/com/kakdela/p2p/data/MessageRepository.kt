package com.kakdela.p2p.data

import com.kakdela.p2p.db.ChatMessage
import com.kakdela.p2p.db.ChatMessageDao
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val dao: ChatMessageDao) {

    suspend fun sendMessage(chatId: String, author: String, text: String) {
        val msg = ChatMessage(
            chatId = chatId,
            author = author,
            text = text
        )
        dao.insert(msg)
    }

    fun observeChat(chatId: String): Flow<List<ChatMessage>> =
        dao.observeChat(chatId)

    fun observeAll(): Flow<List<ChatMessage>> =
        dao.observeAll()
}
