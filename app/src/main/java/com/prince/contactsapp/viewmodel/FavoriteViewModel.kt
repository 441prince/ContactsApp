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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteViewModel(
    private val contactRepository: ContactRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // Define a LiveData to trigger navigation
    private val navigateToNewActivity = MutableLiveData<Boolean>()
    private val favImageButton = MutableLiveData<Int>()

    val updatedFavoriteContactList = MutableLiveData<List<Contact>>()

    // Declare profileId as a MutableLiveData so you can update it
    private val profileId = MutableLiveData<Long>()

    // Observe profileId and update profileContacts using switchMap
    val favoriteContacts: LiveData<List<Contact>> = profileId.switchMap { id ->
        liveData {
            val contacts = contactRepository.getFavoriteContacts(id)
            emitSource(contacts)
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
            Log.d("FavoriteContactViewModel", "Selected Profile: $selectedProfile")
            // Update the profileId LiveData with the selected profile ID
            selectedProfile?.let {
                profileId.value = it.id
            }
        }
    }

    fun updateContactAndNotify(contact: Contact) {
        viewModelScope.launch {
            // Update the contact in the Room database
            contactRepository.update(contact)
            profileId.value = contact.profileId

            /*// Notify the LiveData on the main thread
            // Notify the LiveData on the main thread
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

    fun getAllFavContact() = liveData {
        //insertContact(Contact(6, "123456", "Joel", R.drawable.filledheart))
        contactRepository.allContacts.collect {
            emit(it)
        }
    }

    fun getUpdatedFavoriteContacts(id: Long) {
        viewModelScope.launch {
            val updatedFavoriteContacts = withContext(Dispatchers.IO) {
                contactRepository.getUpdatedFavoriteContacts(id)
            }
            Log.d("getUpdatedFavoriteContacts", "profileid = $id")
            updatedFavoriteContacts.let {
                updatedFavoriteContactList.value = it
                Log.d("UpdatedFavContacts", "list = $it")
                //Log.d("getUpdatedFavoriteContacts", "it = ${it.size}")
            }
        }
    }
    fun insertContact(contact: Contact) = viewModelScope.launch {
        contactRepository.insert(contact)
    }
}


class FavoriteViewModelFactory(
    private val contactRepository: ContactRepository,
    private val profileRepository: ProfileRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(contactRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}