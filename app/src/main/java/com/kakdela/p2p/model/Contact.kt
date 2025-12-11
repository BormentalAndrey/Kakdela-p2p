package com.kakdela.p2p.model

import androidx.compose.runtime.mutableStateMapOf

data class Contact(
    val peerId: String,           // KAKDELA_abcd1234 — из QR
    var displayName: String,      // "Мама", "Друг Вася" — то, что задаёт пользователь
    var isOnline: Boolean = false
)

// Глобальное хранилище контактов (вместо TrustedPeersManager)
object ContactsRepository {
    private val _contacts = mutableStateMapOf<String, Contact>()
    val contacts: Map<String, Contact> = _contacts

    fun addOrUpdate(peerId: String, displayName: String) {
        _contacts[peerId] = Contact(peerId, displayName)
    }

    fun rename(peerId: String, newName: String) {
        _contacts[peerId]?.let { it.displayName = newName }
    }

    fun getById(peerId: String) = _contacts[peerId]
}
