package com.kakdela.p2p.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts")
    suspend fun getAllSuspend(): List<Contact>

    @Insert
    suspend fun insert(contact: Contact)
}
