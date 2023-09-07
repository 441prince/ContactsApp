package com.prince.contactsapp.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prince.contactsapp.models.ContactRepository
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.models.ProfileRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class AddViewEditProfileViewModel(
    private val application: Application,
    private val profileRepository: ProfileRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    val inputName = MutableLiveData<String>()
    val navigateToAnotherActivity = MutableLiveData<Boolean>()
    val displayName = MutableLiveData<String>()
    val displayImageUri: MutableLiveData<String?> = MutableLiveData()
    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    private val PICK_IMAGE = 1
    private val CAPTURE_IMAGE = 2
    private var profileId: Long = 0
    private var isDefault: Boolean = false
    private var isSelected: Boolean = false


    /*val inputPhoneNumber = MutableLiveData<String>()
    val inputEmailId = MutableLiveData<String>()*/
    /*val displayPhoneNumber = MutableLiveData<String>()
    val displayEmailId = MutableLiveData<String>()*/


    private fun insertProfile(profile: Profile) = viewModelScope.launch {
        try {
            _errorMessage.value = null // Clear any previous error message
            profileRepository.insert(profile)
        } catch (ex: SQLiteConstraintException) {
            // Handle the case of a duplicate phone number here
            //Log.d("ADddViewEditProfileViewModelf", "Profile name already exists")
            _errorMessage.postValue("Profile name already exists") // Set your error message
        }
    }

    private fun updateProfile(profile: Profile) = viewModelScope.launch {
        try {
            _errorMessage.value = null // Clear any previous error message
            profileRepository.update(profile)
        } catch (ex: SQLiteConstraintException) {
            // Handle the case of a duplicate phone number here
            _errorMessage.postValue("Profile name already exists") // Set your error message
        }
    }

    fun deleteProfile() = viewModelScope.launch {
        // Delete the contact using the contactId
        profileRepository.deleteProfileById(profileId)
        contactRepository.deleteContactsByProfileId(profileId)

        // Optionally, navigate back to the previous screen or perform other actions as needed
        navigateToAnotherActivity.value = true
    }


    fun editOrUpdateProfileButton() {
        val name = inputName.value?.trim()

        if (name.isNullOrEmpty()) {
            _errorMessage.value = "Name cannot be empty."
            return
        } else if (inputName.value != null) {
            if (selectedImageUri.value != null) {
                displayImageUri.value = selectedImageUri.value.toString()
            }
            val profile = Profile(
                id = profileId,
                name = name,
                imageUri = displayImageUri.value, // Convert Uri to String
                isDefault = isDefault,
                isSelected = isSelected
            )
            updateProfile(profile)
            Log.d("AVEPVM editOrUpdateButton() if", "This is a debug message.$profileId")
            navigateToAnotherActivity.value = true
        } else {
            Log.d("AVEPVM editOrUpdateButton() else", "This is a debug message.$profileId")
        }
    }

    fun addProfile() {
        val name = inputName.value?.trim()

        if (name.isNullOrEmpty()) {
            _errorMessage.value = "Name cannot be empty."
            return
        } else if (inputName.value != null) {
            val profile = Profile(
                id = 0,
                name = name,
                imageUri = selectedImageUri.value.toString(), // Convert Uri to String
                isDefault = false,
                isSelected = false
            )

            insertProfile(profile)
            // Perform some actions, and then trigger navigation
            navigateToAnotherActivity.value = true
        }
    }

    fun displayProfile(profileID: Long) = viewModelScope.launch {
        val profile = profileRepository.getProfileById(profileID)
        if (profile != null) {
            profileId = profile.id
            displayName.value = profile.name
            displayImageUri.value = profile.imageUri
            inputName.value = profile.name
            displayImageUri.value = profile.imageUri
            isDefault = profile.isDefault
            isSelected = profile.isSelected

        } else {
            // Handle the case where the contact with the provided phone number doesn't exist
        }

    }

    fun pickImageFromGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }

        if (photoFile != null) {
            val photoUri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.provider",
                photoFile
            )
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
            activity.startActivityForResult(intent, PICK_IMAGE)
        }
    }

    // Modify this function to copy the picked image to the app's external files directory
    fun copyPickedImageToAppDirectory(pickedImageUri: Uri) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = application.getExternalFilesDir(null)

        try {
            if (storageDir != null) {
                val imageFile = File(storageDir, "JPEG_${timeStamp}.jpg")
                try {
                    val inputStream = application.contentResolver.openInputStream(pickedImageUri)
                    val outputStream = FileOutputStream(imageFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    _selectedImageUri.value = Uri.fromFile(imageFile)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            } else {
                throw IOException("External storage directory is null or not available.")
            }
        } catch (ex: IOException) {
            // Handle the exception here, you can log it or show an error message
            ex.printStackTrace()
        }

    }


    fun captureImage(activity: Activity) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            if (photoFile != null) {
                val photoUri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.provider",
                    photoFile
                )
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
                activity.startActivityForResult(intent, CAPTURE_IMAGE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = application.getExternalFilesDir(null)

        if (storageDir != null) {
            val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            _selectedImageUri.value = Uri.fromFile(imageFile)
            return imageFile
        } else {
            throw IOException("External storage directory is null or not available.")
        }
    }


    fun setImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }
}

class AddViewEditProfileViewModelFactory(
    private val application: Application,
    private val profileRepository: ProfileRepository,
    private val contactRepository: ContactRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddViewEditProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddViewEditProfileViewModel(application, profileRepository, contactRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}