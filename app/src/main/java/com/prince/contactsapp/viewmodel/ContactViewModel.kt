package com.prince.contactsapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.prince.contactsapp.R
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.ContactRepository
import com.prince.contactsapp.models.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ContactViewModel(
    private val contactRepository: ContactRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // Declare profileId as a MutableLiveData so you can update it
    private val profileId = MutableLiveData<Long>()

    // Create a MutableLiveData to hold the search query
    private val searchQuery = MutableLiveData<String>()

    // Observe profileId and update profileContacts using switchMap
    val profileContacts: LiveData<List<Contact>> = profileId.switchMap { id ->
        liveData {
            val contacts = contactRepository.getAllContactsByProfileId(id)
            emitSource(contacts)
        }
    }

    // Observe searchQuery and update searchResults using switchMap
    val searchResults: LiveData<List<Contact>> = searchQuery.switchMap { query ->
        liveData {
            // Get the current profile ID from profileId LiveData
            val currentProfileId = profileId.value

            if (currentProfileId != null) {
                // Use the current profile ID and query to search for contacts
                val results = contactRepository.searchContactsByProfileId(currentProfileId, query)
                emitSource(results)
            }
        }
    }

    // Initialize profileId in the constructor
    init {
        // Launch a coroutine in the viewModelScope to fetch the selected profile
        //fun getSelectedProfile() =
        viewModelScope.launch {
            val selectedProfile = withContext(Dispatchers.IO) {
                profileRepository.getSelectedProfile()
            }
            Log.d("ContactViewModel", "Selected Profile: $selectedProfile")
            // Update the profileId LiveData with the selected profile ID
            selectedProfile?.let {
                profileId.value = it.id
            }
        }
    }

    //val profileContacts: LiveData<List<Contact>> = contactRepository.getAllContactsByProfileId(profileId)

    // Define a LiveData to trigger navigation
    private val navigateToNewActivity = MutableLiveData<Boolean>()
    private val favImageButton = MutableLiveData<Int>()
    fun updateContactAndNotify(contact: Contact) {
        viewModelScope.launch {
            // Update the contact in the Room database
            contactRepository.update(contact)

            /*// Notify the LiveData on the main thread
            withContext(Dispatchers.Main) {
                notifyRepositoryEvent(
                    ContactRepository.RepositoryEvent(
                        ContactRepository.RepositoryEvent.Action.NOTIFY_DATA_SET_CHANGED
                    )
                )
            }*/
        }
    }

    fun getNavigateToNewActivity(): LiveData<Boolean>? {
        return navigateToNewActivity
    }

    // Method to handle button click and trigger navigation
    fun onPlusButtonClick() {
        navigateToNewActivity.value = true
    }

    fun onFavoriteButtonClick() {
        if (favImageButton.value == R.drawable.emptyheart) {
            favImageButton.value = R.drawable.filledheart
        } else {
            favImageButton.value = R.drawable.emptyheart
        }
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */

    fun getAllContact() = liveData {
        //insertContact(Contact(6, "123456", "Joel", R.drawable.filledheart))
        contactRepository.allContacts.collect {
            emit(it)
        }
    }

    // Function to set the search query and trigger search
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun insertContact(contact: Contact) = viewModelScope.launch {
        contactRepository.insert(contact)
    }
}

class ContactViewModelFactory(
    private val contactRepository: ContactRepository,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(contactRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
