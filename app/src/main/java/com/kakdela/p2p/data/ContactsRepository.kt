package com.kakdela.p2p.data

import com.kakdela.p2p.data.db.Contact
import com.kakdela.p2p.data.db.ContactsDao

class ContactsRepository(
    private val dao: ContactsDao
) {
    suspend fun add(peerId: String, name: String) {
        dao.insert(Contact(peerId, name))
    }

    suspend fun getAll() = dao.getAll()
}
