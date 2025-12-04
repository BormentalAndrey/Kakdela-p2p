package com.kakdela.p2p.data

import com.kakdela.p2p.db.ChatMessage
import com.kakdela.p2p.db.MessageDao
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val dao: MessageDao) {
    fun messagesForChat(chatId: String): Flow<List<ChatMessage>> = dao.messagesForChat(chatId)
    suspend fun sendMessage(msg: ChatMessage) = dao.insert(msg)
}
