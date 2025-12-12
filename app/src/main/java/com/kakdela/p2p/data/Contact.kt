package com.kakdela.p2p.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val displayName: String = "Unnamed",
    val publicKeyHex: String? = null // placeholder for crypto identity
)
