package com.prince.contactsapp.models

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    /*// Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allWords: Flow<List<Word>> = contactDao.getAlphabetizedWords()*/

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(contact: Contact) {
        contactDao.insertContact(contact)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(contact: Contact) {
        contactDao.updateContact(contact)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteContactById(contactId: Long) {
        contactDao.deleteContactById(contactId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteContactsByProfileId(profileId: Long) {
        contactDao.deleteContactsByProfileId(profileId)
    }

    // Function to get a contact by phone number
    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact? {
        return contactDao.getContactByPhoneNumber(phoneNumber)
    }
    suspend fun getContactByPhoneNumberAndProfileId(phoneNumber: String, profileId: Long): Contact? {
        return contactDao.getContactByPhoneNumberAndProfileId(phoneNumber, profileId)
    }

    fun getFavoriteContacts(profileId: Long): LiveData<List<Contact>> {
        return contactDao.getFavoriteContacts(profileId)
    }

    val allContacts: Flow<List<Contact>> = contactDao.getAllContact()

    fun getAllContactsByProfileId(profileId: Long): LiveData<List<Contact>> {
        return contactDao.getAllContactByProfileId(profileId)
    }

    fun searchContactsByProfileId(profileId: Long, query: String): LiveData<List<Contact>> {
        return contactDao.searchContactsByProfileId(profileId, "%$query%") // Modify the query as needed
    }
}