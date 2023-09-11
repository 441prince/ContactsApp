package com.prince.contactsapp.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.prince.contactsapp.models.AppDatabase
import com.prince.contactsapp.models.ContactRepository
import com.prince.contactsapp.models.ProfileRepository
import com.prince.contactsapp.view.ui.theme.ContactsAppTheme
import com.prince.contactsapp.viewmodel.AddViewEditProfileViewModel
import com.prince.contactsapp.viewmodel.AddViewEditProfileViewModelFactory


@Preview
@Composable
fun AddViewEditProfileScreenPreview() {
    AddViewEditProfileActivity
}

class AddViewEditProfileActivity : ComponentActivity() {

    private lateinit var addViewEditProfileViewModel: AddViewEditProfileViewModel
    private var refreshState by mutableIntStateOf(0)
    var selectedImageUri : Uri? = null

    private val PICK_IMAGE = 1
    private val CAPTURE_IMAGE = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileDao = AppDatabase.getDatabase(this).ProfileDao()
        val profileRepository = ProfileRepository(profileDao)
        val contactDao = AppDatabase.getDatabase(this).ContactDao()
        val contactRepository = ContactRepository(contactDao)
        val addViewEditProfileViewModelFactory =
            AddViewEditProfileViewModelFactory(application, profileRepository, contactRepository)
        addViewEditProfileViewModel =
            ViewModelProvider(this, addViewEditProfileViewModelFactory).get(
                AddViewEditProfileViewModel::class.java
            )
        var profileID : Long? = null

        // In the target activity where you want to retrieve the extra:
        val extras = intent.extras
        if (extras != null) {
            profileID = extras.getString("profile_id")?.toLong()
            Toast.makeText(this, extras.getString("profile_id"), Toast.LENGTH_SHORT).show()
            if (profileID != null) {
                addViewEditProfileViewModel.displayProfile(profileID!!)
                selectedImageUri = addViewEditProfileViewModel.displayImageUri.value?.toUri()
                //Toast.makeText(this,"$selectedImageUri", Toast.LENGTH_SHORT).show()

            } else {
                //binding.AddContactSubmitButton.visibility = View.VISIBLE
                //binding.EditOrUpdateProfileLayout.visibility = View.GONE

                // Set a click listener for yourImageView to open the image picker
            }
        } else {
            // Handle the case where the extra was not passed or is null.
            //binding.selectProfileImage.setOnClickListener {
            //checkPermissionAndPickImage()
        }

        // Observe the selectedImageUri LiveData
        addViewEditProfileViewModel.displayImageUri.observe(this, Observer { uri ->
            uri?.let {
                // Display the selected image using an ImageView or load it using Glide
                this.selectedImageUri = uri.toUri()
                //Toast.makeText(this,"observed $selectedImageUri", Toast.LENGTH_SHORT).show()
                updateRefreshState(refreshState)
            }
        })

        addViewEditProfileViewModel.navigateToAnotherActivity.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Reset the LiveData value to prevent repeated navigation
                addViewEditProfileViewModel.navigateToAnotherActivity.value = false

                //onBackPressed();
                // Create an Intent to navigate to another activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

        setContent {
            ContactsAppTheme {
                // Create a NavHostController
                val navController = rememberNavController()
                AddViewEditProfileScreen(
                    addViewEditProfileViewModel,
                    profileID,
                    refreshState,
                    selectedImageUri,
                    this
                )
            }
        }
    }

    fun checkPermissionAndPickImage() {
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
                0 -> addViewEditProfileViewModel.captureImage(this)
                1 -> addViewEditProfileViewModel.pickImageFromGallery(this)
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    companion object {
        private const val PERMISSION_CODE = 1001
    }

    private fun updateRefreshState(state: Int) {
        if (state<10) {
            refreshState ++
        } else if (state>=10) {
            refreshState = 0
        }
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
                            addViewEditProfileViewModel.copyPickedImageToAppDirectory(pickedImageUri)
                            this.selectedImageUri = pickedImageUri
                            updateRefreshState(refreshState)
                            //Toast.makeText(this, "$selectedImageUri", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                CAPTURE_IMAGE -> {
                    // Image captured from camera, use the selectedImageUri from the ViewModel
                    addViewEditProfileViewModel.selectedImageUri.value?.let { selectedImageUri ->
                        this.selectedImageUri = selectedImageUri
                        updateRefreshState(refreshState)
                        //Toast.makeText(this, "$selectedImageUri", Toast.LENGTH_SHORT).show()
                        // You can use the selectedImageUri to display the captured image
                        // For example, with Glide or setImageURI on an ImageView
                        //binding.selectProfileImage.setImageURI(selectedImageUri)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddViewEditProfileScreen(
    addViewEditProfileViewModel: AddViewEditProfileViewModel,
    profileId: Long?,
    refreshState: Int,
    selectedImageUri: Uri?,
    addViewEditProfileActivity: AddViewEditProfileActivity
) {
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(profileId == null) }


    val selectedImagePainter = rememberAsyncImagePainter(selectedImageUri)

    /*// Observe the selectedImageUri LiveData
    addViewEditProfileViewModel.selectedImageUri.observe(addViewEditProfileActivity, Observer { uri ->
        uri?.let {
            Toast.makeText(addViewEditProfileActivity, "uri 1", Toast.LENGTH_SHORT).show()
            selectedImageUri = uri
        }
    })*/


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (profileId == null) "Add New Profile" else if(isEditing) "Update Profile" else "Profile Details") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            //navController.popBackStack()
                            //onBackPressed()
                            addViewEditProfileActivity.onBackPressed()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        content = { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding), // Apply contentPadding to push the content below the top app bar
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileImage(
                    imageUri = selectedImageUri,
                    painter = selectedImagePainter,
                    isEditing = isEditing,
                    onImageClicked = { if (isEditing || profileId == null) addViewEditProfileActivity.checkPermissionAndPickImage() },
                    profileId
                )

                //val profileName =
                var onProfileNameChange: (String) -> Unit = { name ->
                    addViewEditProfileViewModel.inputName.value = name
                }
                var profileName by remember { mutableStateOf(TextFieldValue(text = addViewEditProfileViewModel.inputName.value ?: "")) }

                ProfileNameField(
                    profileName = profileName,
                    onProfileNameChange = {
                        addViewEditProfileViewModel.inputName.value = it.text
                        profileName = it},
                    isEditing = isEditing
                )

                if (isEditing || profileId == null) {
                    EditableProfileButtons(
                        onSaveButtonClick = {
                            if (profileId == null) {
                                addViewEditProfileViewModel.addProfile()
                            } else {
                                addViewEditProfileViewModel.editOrUpdateProfileButton()
                            }
                            //navController.popBackStack()
                        }
                    )
                } else {
                    NonEditableProfileButtons(
                        onEditButtonClick = {
                            isEditing = true
                        }
                    )
                }

                if (profileId != null) {
                    DeleteProfileButton(
                        onDeleteButtonClick = {
                            addViewEditProfileViewModel.deleteProfile()
                        }
                    )
                }

                addViewEditProfileViewModel.errorMessage.value?.let { errorMessage ->
                    Text(text = errorMessage, color = Color.Red)
                }
            }
        }
    )
}


@Composable
fun ProfileImage(
    imageUri: Uri?,
    painter: Painter,
    isEditing: Boolean,
    onImageClicked: () -> Unit,
    profileId: Long?
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
            .clickable { onImageClicked() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )
        }

        if (isEditing) {
            Text(
                text = if (profileId == null ) "Click to Add Image" else "Click to Update Image",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileNameField(
    profileName: TextFieldValue,
    onProfileNameChange: (TextFieldValue) -> Unit,
    isEditing: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    if (isEditing) {
        BasicTextField(
            value = profileName,
            onValueChange = { onProfileNameChange(it) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
                .background(Color.White)
                .padding(8.dp)
        )
    } else {
        Text(
            text = profileName.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EditableProfileButtons(
    onSaveButtonClick: () -> Unit
) {
    Button(
        onClick = onSaveButtonClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Save")
    }
}

@Composable
fun NonEditableProfileButtons(
    onEditButtonClick: () -> Unit
) {
    Button(
        onClick = onEditButtonClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Edit")
    }
}

@Composable
fun DeleteProfileButton(
    onDeleteButtonClick: () -> Unit
) {
    Button(
        onClick = onDeleteButtonClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Delete")
    }
}

/*class AddViewEditProfileActivity : AppCompatActivity() {

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
}*/