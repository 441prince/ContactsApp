package com.prince.contactsapp.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.ContactRepository
import com.prince.contactsapp.models.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class AddNewContactViewModel(
    private val application: Application,
    private val contactRepository: ContactRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val inputName = MutableLiveData<String>()
    val inputPhoneNumber = MutableLiveData<String>()
    val inputEmailId = MutableLiveData<String>()
    val navigateToAnotherActivity = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    private val PICK_IMAGE = 1
    private val CAPTURE_IMAGE = 2

    private var existingContact: Contact? = null

    fun insertContact(contact: Contact) = viewModelScope.launch {
        try {
            _errorMessage.value = null // Clear any previous error message
            if (existingContact == null) {
                // No duplicate found, you can proceed with the insertion.
                contactRepository.insert(contact)
            } else {
                // Duplicate contact found for the same profile, handle accordingly.
                _errorMessage.postValue("Phone number already exists for this profile.")
            }
        } catch (ex: SQLiteConstraintException) {
            // Handle the case of a duplicate phone number here
            _errorMessage.postValue("Phone number already exists.") // Set your error message
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Add your phone number validation logic here
        // For example, you can use regular expressions to validate the format.
        // For a simple example, let's assume a valid phone number has at least 10 digits.
        return phoneNumber.length >= 10
    }

    private fun isValidEmail(email: String): Boolean {
        // Add your email validation logic here
        // You can use regular expressions to validate the email format.
        // For a simple example, let's assume a valid email has an "@" character.
        return email.contains("@")
    }

    fun addContact() {
        val name = inputName.value?.trim()
        val phoneNumber = inputPhoneNumber.value?.trim()
        val email = inputEmailId.value?.trim()

        if (name.isNullOrEmpty()) {
            _errorMessage.value = "Name cannot be empty."
            return
        } else if (phoneNumber.isNullOrEmpty()) {
            _errorMessage.value = "Phone number cannot be empty."
            return
        } else if (!isValidPhoneNumber(phoneNumber)) {  // Add more validation for phone number format if needed
            _errorMessage.value = "Invalid phone number format."
            return
        } else if (email.isNullOrEmpty()) {
            _errorMessage.value = "Email cannot be empty."
            return
        } else if (!isValidEmail(email)) {  // Add more validation for email format if needed
            _errorMessage.value = "Invalid email format."
            return
        } else if (inputName.value != null && inputPhoneNumber.value != null && inputEmailId.value != null) {

            viewModelScope.launch {
                val selectedProfile = withContext(Dispatchers.IO) {profileRepository.getSelectedProfile() }
                val currentProfileId = selectedProfile.id
                val contact = Contact(
                    id = 0,
                    phoneNumber = phoneNumber,
                    name = name,
                    emailId = email,
                    imageUri = selectedImageUri.value.toString(), // Convert Uri to String
                    isFavorite = false,
                    profileId = currentProfileId
                )
                existingContact = contactRepository.getContactByPhoneNumberAndProfileId(inputPhoneNumber.value!!, currentProfileId)
                insertContact(contact)
            }
            // Perform some actions, and then trigger navigation
            navigateToAnotherActivity.value = true
        } else {
            Log.d("AddNewContactViewModel", "This is a debug message.")
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
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
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

class AddNewContactViewModelFactory(
    private val application: Application,
    private val contactRepository: ContactRepository,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddNewContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddNewContactViewModel(application, contactRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}