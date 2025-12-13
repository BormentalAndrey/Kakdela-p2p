package com.kakdela.p2p.data

import com.kakdela.p2p.App
import com.kakdela.p2p.db.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

object MessageRepository {

    private val dao = App.instance.database.messageDao()

    suspend fun insert(message: ChatMessageEntity) {
        dao.insert(message)
    }

    fun observeChat(peerId: String): Flow<List<ChatMessageEntity>> {
        return dao.observeChat(peerId)
    }
}
