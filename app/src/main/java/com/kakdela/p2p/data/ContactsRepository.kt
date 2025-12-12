package com.kakdela.p2p.data

// Very small repository: in-memory list for quick start. Later move to Room.
class ContactsRepository {
    private val list = mutableListOf<Contact>()

    init {
        // sample
        list.add(Contact(displayName = "Alice"))
        list.add(Contact(displayName = "Bob"))
    }

    fun getAll(): List<Contact> = list.toList()

    fun add(contact: Contact): String {
        list.add(contact)
        return contact.id
    }
}
