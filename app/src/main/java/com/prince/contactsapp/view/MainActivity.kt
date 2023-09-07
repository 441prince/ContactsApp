package com.prince.contactsapp.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import com.prince.contactsapp.models.AppDatabase
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.models.ProfileRepository
import com.prince.contactsapp.view.ui.theme.ContactsAppTheme
import com.prince.contactsapp.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.ContactRepository
import com.prince.contactsapp.viewmodel.ContactViewModel
import com.prince.contactsapp.viewmodel.ContactViewModelFactory
import com.prince.contactsapp.viewmodel.ProfileViewModelFactory


@AndroidEntryPoint
class MainActivity : ComponentActivity(), ItemClickListener {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var contactViewModel: ContactViewModel
    private val profileList: ArrayList<Profile> = ArrayList() // Initialize an empty list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileDao = AppDatabase.getDatabase(this).ProfileDao()
        val profileRepository = ProfileRepository(profileDao)
        val contactDao = AppDatabase.getDatabase(this).ContactDao()
        val contactRepository = ContactRepository(contactDao)
        val profileViewModelFactory = ProfileViewModelFactory(profileRepository, contactRepository)
        val contactViewModelFactory = ContactViewModelFactory(contactRepository, profileRepository)
        profileViewModel = ViewModelProvider(this, profileViewModelFactory).get(ProfileViewModel::class.java)
        contactViewModel = ViewModelProvider(this, contactViewModelFactory).get(ContactViewModel::class.java)

        // Define your default profile
        val defaultProfile = Profile(id = 1, name = "Default Profile", isDefault = true, imageUri = null)
        profileViewModel.addDefaultProfile(defaultProfile)


        profileViewModel.getAllProfiles().observe(this, Observer { profiles ->
            // Update the profile count when the profile list changes
            //profileCount = profiles.size
            // Disable the "Create Profile" button if there are already three profiles
            //updateCreateProfileButtonState()
            profileList.clear()
            profileList.addAll(profiles)
        })

        setContent {
            ContactsAppTheme {
                // Create a NavHostController
                val navController = rememberNavController()
                MainActivityContent(navController, profileList, profileViewModel, this)
            }
        }
    }

    override fun onContactClick(contact: Contact) {
        TODO("Not yet implemented")
    }

    override fun onProfileClick(profile: Profile, context: Context) {
        val intent = Intent(context, AddViewEditProfileActivity::class.java)
        // Pass the contact data to the EditContactActivity

        intent.putExtra("profile_id", profile.id.toString())
        //Toast.makeText(requireContext(), "${profile.id}", Toast.LENGTH_SHORT).show()
        // Start the EditContactActivity
        startActivity(intent)

    }

    override fun onProfileLongClick(profile: Profile) {
        TODO("Not yet implemented")
    }
}

@Composable
fun MainActivityContent(
    navController: NavHostController,
    profiles: List<Profile>,
    profileViewModel: ProfileViewModel,
    mainActivity: MainActivity) {
    ContactsAppTheme {
        // Set up the NavHost with tabs
        NavHost(
            navController = navController,
            startDestination = "profiles"
        ) {
            composable("profiles") {
                /*ProfileTab(
                    navController = navController,
                    profiles = profiles,
                    profileViewModel = profileViewModel,
                    itemClickListener = mainActivity
                )*/
            }
            composable("contacts") {
                ContactTab(navController)
            }
            composable("favorites") {
                FavoriteTab(navController)
            }

            composable("addProfile") {
                //AddProfileScreen(navController = navController)
            }
        }

        MainContent(navController, profiles, profileViewModel, mainActivity)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    navController: NavController,
    profiles: List<Profile>,
    profileViewModel: ProfileViewModel,
    mainActivity: MainActivity
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        // Create a TopAppBar with the app name

        // Define the tabs
        val tabTitles = listOf("profiles", "contacts", "favorites")
        val (appBar, tabRow, content) = createRefs() // Create references for the TopAppBar, TabRow, and content

        // Create a TopAppBar
        TopAppBar(
            title = { Text(text = "Contacts") },
            navigationIcon = {},
            actions = {},
            //elevation = 0.dp, // Remove elevation if needed
            //colors = TopAppBarColors(containerColor = Color.Blue),
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
            ),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .constrainAs(appBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        // Create a guideline for the top edge of the TabRow
        //val tabRowGuideline = createGuidelineFromTop(0.08f) // Adjust the top guideline as needed
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "profiles"

        // Create the TabRow for navigation
        TabRow(
            selectedTabIndex = tabTitles.indexOf(currentRoute), // Update the selectedTabIndex
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    color = Color.White,
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabTitles.indexOf(currentRoute)])
                )
            },
            tabs = {
                tabTitles.forEachIndexed { index, label ->
                    Tab(
                        selected = index == tabTitles.indexOf(currentRoute),
                        onClick = {
                            navController.navigate(label)
                        },
                        text = {
                            Text(text = label.capitalize())
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .constrainAs(tabRow) {
                    //top.linkTo(tabRowGuideline) // Place TabRow below the TopAppBar
                    top.linkTo(appBar.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            //backgroundColor = MaterialTheme.colorScheme.primary.
        )

        val tabContentGuideline = createGuidelineFromTop(0.15f) // Adjust the top guideline as needed
        // Content for the selected tab
        Column(
           /* modifier = Modifier
                .constrainAs(content) {
                    top.linkTo(tabContentGuideline) // Place content below the TabRow
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    //bottom.linkTo(parent.bottom) // Align content with the bottom of the screen
                }*/
            modifier = Modifier.constrainAs(content) {
                top.linkTo(tabContentGuideline)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                //bottom.linkTo(parent.bottom)
            }.fillMaxSize(),
            //verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (navController.currentDestination?.route) {
                "profiles" -> {
                    ProfileTab(navController, profiles, profileViewModel, mainActivity)
                }
                "contacts" -> {
                    ContactTab(navController)
                }
                "favorites" -> {
                    FavoriteTab(navController)
                }
                else -> {
                    // Handle other cases or leave it empty
                }
            }
        }
    }
}


@Composable
fun ContactTab(navController: NavController) {
    Text("Contacts Tab Content")
}

@Composable
fun FavoriteTab(navController: NavController) {
    Text("Favorites Tab Content")
}

@Preview(showBackground = true)
@Composable
fun ContactTabsPreview() {
    ContactsAppTheme { // Use the ContactsTheme here
        MainActivity()
        //ContactTabs()
    }
}




