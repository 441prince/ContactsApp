package com.prince.contactsapp.models

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact_table ORDER BY contactName ASC")
    fun getAllContact(): Flow<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE profileId = :profileId ORDER BY contactName ASC")
    fun getAllContactByProfileId(profileId: Long): LiveData<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE contactNumber = :phoneNumber")
    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact?

    @Query("SELECT * FROM contact_table WHERE contactNumber = :phoneNumber AND profileId = :profileId")
    suspend fun getContactByPhoneNumberAndProfileId(phoneNumber: String, profileId: Long): Contact?


    @Query("SELECT * FROM contact_table WHERE isFavorite = 1 AND profileId = :profileId ORDER BY contactName ASC")
    fun getFavoriteContacts(profileId: Long): LiveData<List<Contact>>

    @Insert
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM contact_table WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: Long)

    @Query("DELETE FROM contact_table WHERE profileId = :profileId")
    suspend fun deleteContactsByProfileId(profileId: Long)

    @Query("DELETE FROM contact_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM contact_table WHERE profileId = :profileId AND (contactNumber LIKE :query OR contactName LIKE :query)")
    fun searchContactsByProfileId(profileId: Long, query: String): LiveData<List<Contact>>
}