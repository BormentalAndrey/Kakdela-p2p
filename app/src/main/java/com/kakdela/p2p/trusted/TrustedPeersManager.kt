package com.kakdela.p2p.trusted

import com.kakdela.p2p.model.Contact

object TrustedPeersManager {
    private val peers = mutableMapOf<String, Contact>()

    fun addPeer(contact: Contact) {
        peers[contact.peerId] = contact
    }

    fun getById(peerId: String): Contact? = peers[peerId]

    fun getAll(): List<Contact> = peers.values.toList()

    fun rename(peerId: String, newName: String) {
        peers[peerId]?.let { peers[peerId] = it.copy(displayName = newName) }
    }
}
