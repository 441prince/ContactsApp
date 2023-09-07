package com.prince.contactsapp.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.prince.contactsapp.R
import com.prince.contactsapp.databinding.ActivityAddViewEditProfileBinding
import com.prince.contactsapp.models.AppDatabase
import com.prince.contactsapp.models.ContactRepository
import com.prince.contactsapp.models.ProfileRepository
import com.prince.contactsapp.viewmodel.AddViewEditProfileViewModel
import com.prince.contactsapp.viewmodel.AddViewEditProfileViewModelFactory

class AddViewEditProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: AddViewEditProfileViewModel
    private lateinit var binding: ActivityAddViewEditProfileBinding
    private val PICK_IMAGE = 1
    private val CAPTURE_IMAGE = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_view_edit_profile)
        val profileDao = AppDatabase.getDatabase(application).ProfileDao()
        val profileRepository = ProfileRepository(profileDao)
        val contactDao = AppDatabase.getDatabase(application).ContactDao()
        val contactRepository = ContactRepository(contactDao)
        val factory = AddViewEditProfileViewModelFactory(application, profileRepository, contactRepository)
        viewModel = ViewModelProvider(this, factory).get(AddViewEditProfileViewModel::class.java)
        binding.addViewEditProfileViewModel = viewModel
        binding.lifecycleOwner = this

        // In the target activity where you want to retrieve the extra:
        val extras = intent.extras
        if (extras != null) {
            val profileID = extras.getString("profile_id")?.toLong()
            Toast.makeText(this, extras.getString("profile_id"), Toast.LENGTH_SHORT).show()
            if (profileID != null) {
                viewModel.displayProfile(profileID!!)
                binding.AddContactSubmitButton.visibility = View.GONE
                binding.EditOrUpdateProfileLayout.visibility = View.VISIBLE
                binding.EditOrUpdateProfileSubmitButton.text = "Edit"
                binding.editNameText.visibility = View.GONE
                binding.ViewNameText.visibility = View.VISIBLE
                binding.textView.visibility = View.GONE

            } else {
                binding.AddContactSubmitButton.visibility = View.VISIBLE
                binding.EditOrUpdateProfileLayout.visibility = View.GONE

                // Set a click listener for yourImageView to open the image picker
            }
        } else {
            // Handle the case where the extra was not passed or is null.
            binding.selectProfileImage.setOnClickListener {
                checkPermissionAndPickImage()
            }
        }
        /*// Find the Toolbar in your layout
        val toolbar: Toolbar = findViewById(R.id.toolbar)*/

        // Set the Toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        // Set the title for the ActionBar
        supportActionBar?.title = "Add New Profile" // Replace with your desired title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Observe the selectedImageUri LiveData
        viewModel.displayImageUri.observe(this, Observer { uri ->
            uri?.let {
                // Display the selected image using an ImageView or load it using Glide
                Glide.with(this)
                    .load(uri)
                    .centerCrop() // Center-crop the image within the circular frame
                    .into(binding.selectProfileImage)
            }
        })

        // Observe the selectedImageUri LiveData
        viewModel.selectedImageUri.observe(this, Observer { uri ->
            uri?.let {
                // Display the selected image using an ImageView or load it using Glide
                Glide.with(this)
                    .load(uri)
                    .centerCrop() // Center-crop the image within the circular frame
                    .into(binding.selectProfileImage)
            }
        })
        binding.EditOrUpdateProfileSubmitButton.setOnClickListener {
            if(binding.EditOrUpdateProfileSubmitButton.text == "Edit") {
                binding.EditOrUpdateProfileSubmitButton.text = "Update"
                binding.textView.visibility = View.VISIBLE
                binding.textView.text = "Click to Update Image"
                binding.editNameText.visibility = View.VISIBLE
                binding.ViewNameText.visibility = View.GONE
                binding.selectProfileImage.setOnClickListener {
                    checkPermissionAndPickImage()
                }
            } else {
                viewModel.editOrUpdateProfileButton()
            }
        }



        viewModel.errorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                // Show the error message to the user, e.g., using a Toast or a TextView
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })



        viewModel.navigateToAnotherActivity.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Reset the LiveData value to prevent repeated navigation
                viewModel.navigateToAnotherActivity.value = false

                //onBackPressed();
                // Create an Intent to navigate to another activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the up button click (e.g., navigate back)
                onBackPressed()
                return true
            }
            // Handle other menu items if needed
            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun checkPermissionAndPickImage() {
        val cameraPermission = android.Manifest.permission.CAMERA
        val storagePermission = android.Manifest.permission.READ_EXTERNAL_STORAGE

        val cameraPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            cameraPermission
        ) == PackageManager.PERMISSION_GRANTED
        val storagePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            storagePermission
        ) == PackageManager.PERMISSION_GRANTED

        if (!cameraPermissionGranted || !storagePermissionGranted) {
            // Request permissions for both camera and storage if not granted
            val permissionsToRequest = mutableListOf<String>()
            if (!cameraPermissionGranted) {
                permissionsToRequest.add(cameraPermission)
            }
            if (!storagePermissionGranted) {
                permissionsToRequest.add(storagePermission)
            }
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_CODE
            )
        } else {
            // Both permissions are granted
            openImagePicker()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty()) {
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allPermissionsGranted) {
                // All requested permissions are granted
                openImagePicker()
            } else {
                // Handle the case where not all permissions are granted
                // You can show a message to the user or take appropriate action
            }
        }
    }


    private fun openImagePicker() {
        val options = arrayOf("Camera", "Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> viewModel.captureImage(this)
                1 -> viewModel.pickImageFromGallery(this)
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    companion object {
        private const val PERMISSION_CODE = 1001
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE -> {
                    if (data != null) {
                        val pickedImageUri = data.data
                        // Copy the picked image to the app's directory
                        if (pickedImageUri != null) {
                            viewModel.copyPickedImageToAppDirectory(pickedImageUri)
                        }
                    }
                }

                CAPTURE_IMAGE -> {
                    // Image captured from camera, use the selectedImageUri from the ViewModel
                    viewModel.selectedImageUri.value?.let { selectedImageUri ->
                        // You can use the selectedImageUri to display the captured image
                        // For example, with Glide or setImageURI on an ImageView
                        binding.selectProfileImage.setImageURI(selectedImageUri)
                    }
                }
            }
        }
    }
}