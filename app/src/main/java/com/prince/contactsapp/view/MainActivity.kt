package com.prince.contactsapp.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
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
import com.prince.contactsapp.viewmodel.FavoriteViewModel
import com.prince.contactsapp.viewmodel.FavoriteViewModelFactory
import com.prince.contactsapp.viewmodel.ProfileViewModelFactory


@AndroidEntryPoint
class MainActivity : ComponentActivity(), ItemClickListener {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private val profileList: ArrayList<Profile> = ArrayList() // Initialize an empty list
    private val contactList: ArrayList<Contact> = ArrayList()
    private val favoriteContactList: ArrayList<Contact> = ArrayList()
    private val currentProfileId by mutableIntStateOf(0)

    // MutableState to trigger recomposition
    private var refreshState by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileDao = AppDatabase.getDatabase(this).ProfileDao()
        val profileRepository = ProfileRepository(profileDao)
        val contactDao = AppDatabase.getDatabase(this).ContactDao()
        val contactRepository = ContactRepository(contactDao)
        val profileViewModelFactory = ProfileViewModelFactory(profileRepository, contactRepository)
        val contactViewModelFactory = ContactViewModelFactory(contactRepository, profileRepository)
        val favoriteViewModelFactory =
            FavoriteViewModelFactory(contactRepository, profileRepository)
        profileViewModel =
            ViewModelProvider(this, profileViewModelFactory).get(ProfileViewModel::class.java)
        contactViewModel =
            ViewModelProvider(this, contactViewModelFactory).get(ContactViewModel::class.java)
        favoriteViewModel =
            ViewModelProvider(this, favoriteViewModelFactory).get(FavoriteViewModel::class.java)

        // Define your default profile
        val defaultProfile =
            Profile(id = 1, name = "Default Profile", isDefault = true, imageUri = null)
        profileViewModel.addDefaultProfile(defaultProfile)


        profileViewModel.getAllProfiles().observe(this, Observer { profiles ->
            Log.d("MA getAllProfiles()" ,"observe: ${profiles.size} ")
            refreshState = updateRefreshState(refreshState)
            profileList.clear()
            profileList.addAll(profiles)
        })

        // Update contactList and favoriteContactList when profile data changes
        profileViewModel.selectedProfile.observe(this, Observer { selectedProfile ->
            // Handle the selected profile change here
            if (selectedProfile != null) {
                //Toast.makeText(this, "Profile Switched to ${selectedProfile.name}", Toast.LENGTH_SHORT).show()
                Log.d("MA selectedProfile" ,"observe: ${selectedProfile.id} ")
                contactViewModel.getContactsUpdatedForProfile(selectedProfile.id)
                favoriteViewModel.getUpdatedFavoriteContacts(selectedProfile.id)
            } else {
                // No profile is selected, handle this case as needed
                /*Toast.makeText(this, "No profile is selected", Toast.LENGTH_SHORT).show()*/
            }
        })



        contactViewModel.profileContacts.observe(this, Observer { contacts ->
            contactList.clear()
            contactList.addAll(contacts)
            Log.d("MA profileContacts" ,"observe: ${contacts.size} ")
            /*Toast.makeText(this, "profile contact: ${contacts.size}", Toast.LENGTH_SHORT).show()*/
        })

        contactViewModel.updatedContactList.observe(this, Observer { updatedContacts ->
            contactList.clear()
            contactList.addAll(updatedContacts )
            Log.d("MA updatedContactList" ,"observe: ${updatedContacts.size} ")
            /*Toast.makeText(this, "updated profile contact: ${updatedContacts.size}", Toast.LENGTH_SHORT).show()*/
        })

        // Observe searchResults LiveData
        contactViewModel.searchResults.observe(this, Observer { searchResults ->
            contactList.clear()
            contactList.addAll(searchResults)
            Log.d("MA searchResults" ,"observe: ${searchResults.size} ")
        })

        favoriteViewModel.favoriteContacts.observe(this, Observer { favoriteContacts ->
            favoriteContactList.clear()
            favoriteContactList.addAll(favoriteContacts)
            refreshState = updateRefreshState(refreshState)
            Log.d("MA favoriteContacts" ,"observe: ${favoriteContacts.size} ")
            /*Toast.makeText(this, "Favorite contact: ${favoriteContacts.size} $favoriteContactList", Toast.LENGTH_SHORT).show()*/
        })

        favoriteViewModel.updatedFavoriteContactList.observe(this, Observer { updatedFavoriteContacts ->
            favoriteContactList.clear()
            favoriteContactList.addAll(updatedFavoriteContacts)
            Log.d("MA updatedFavoriteContacts" ,"observe: ${updatedFavoriteContacts.size} ")
            refreshState = updateRefreshState(refreshState)
            /*Toast.makeText(this, "Favorite updated: ${updatedFavoriteContacts.size} $favoriteContactList", Toast.LENGTH_SHORT).show()*/
        })

        setContent {
            ContactsAppTheme {
                // Create a NavHostController
                val navController = rememberNavController()
                MainActivityContent(
                    navController,
                    profileList,
                    profileViewModel,
                    this,
                    contactList,
                    contactViewModel,
                    favoriteContactList,
                    favoriteViewModel,
                    refreshState
                )
            }
        }
    }


    override fun onContactClick(contact: Contact, context: Context) {
        // Create an Intent to open the EditContactActivity
        val intent = Intent(context, ViewOrEditContactActivity::class.java)

        // Pass the contact data to the EditContactActivity
        intent.putExtra("contact_phone", contact.phoneNumber)

        // Start the EditContactActivity
        startActivity(intent)
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
        profileViewModel.selectProfile(profile.id)
        refreshActivity(this)
    }

    override fun onFavoriteContactFavIconClick(contact: Contact, context: Context) {
        //profileViewModel.getSelectedProfile()

        contactViewModel.updateContactAndNotify(contact)
        favoriteViewModel.updateContactAndNotify(contact)
        //contactViewModel.getContactsUpdatedForProfile(contact.profileId)
        //favoriteViewModel.getUpdatedFavoriteContacts(contact.profileId)
        //contactViewModel.getContactsUpdatedForProfile(favoriteContact.profileId)
//        updateRefreshState(refreshState)
//        favoriteViewModel.getUpdatedFavoriteContacts(favoriteContact.profileId)
        //Toast.makeText(this, "onFavoriteContactFavIconClick : ${favoriteContact.profileId} ", Toast.LENGTH_SHORT).show()
        //updateRefreshState(refreshState)

    }

    // Function to refresh the activity
    private fun refreshActivity(mainActivity: MainActivity) {
        //recreate(mainActivity) // Recreate the activity to refresh its content
        //refreshState++
    }
}

private fun updateRefreshState(state: Int): Int {
    var refreshState = state
    if (refreshState<10) {
        refreshState ++
    } else if (refreshState>=10) {
        refreshState = 0
    }
    return refreshState
}

@Composable
fun MainActivityContent(
    navController: NavHostController,
    profiles: List<Profile>,
    profileViewModel: ProfileViewModel,
    mainActivity: MainActivity,
    contacts: List<Contact>,
    contactViewModel: ContactViewModel,
    favoriteContacts: List<Contact>,
    favoriteViewModel: FavoriteViewModel,
    refreshState: Int
) {
    Log.d("MA MainActivityContent" ,"MainActivityContent Called")
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
                //ContactTab(navController)
            }
            composable("favorites") {
                //FavoriteTab(navController)
            }

            composable("addProfile") {
                //AddProfileScreen(navController = navController)
                //updateRefreshState(refreshState)
            }
        }

        MainContent(
            navController,
            profiles,
            profileViewModel,
            mainActivity,
            contacts,
            contactViewModel,
            favoriteContacts,
            favoriteViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    navController: NavController,
    profiles: List<Profile>,
    profileViewModel: ProfileViewModel,
    mainActivity: MainActivity,
    contacts: List<Contact>,
    contactViewModel: ContactViewModel,
    favoriteContacts: List<Contact>,
    favoriteViewModel: FavoriteViewModel
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
                    modifier = Modifier.tabIndicatorOffset(
                        tabPositions[tabTitles.indexOf(
                            currentRoute
                        )]
                    )
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

        val tabContentGuideline =
            createGuidelineFromTop(0.15f) // Adjust the top guideline as needed
        // Content for the selected tab
        Column(
            /* modifier = Modifier
                 .constrainAs(content) {
                     top.linkTo(tabContentGuideline) // Place content below the TabRow
                     start.linkTo(parent.start)
                     end.linkTo(parent.end)
                     //bottom.linkTo(parent.bottom) // Align content with the bottom of the screen
                 }*/
            modifier = Modifier
                .constrainAs(content) {
                    top.linkTo(tabContentGuideline)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    //bottom.linkTo(parent.bottom)
                }
                .fillMaxSize(),
            //verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (navController.currentDestination?.route) {
                "profiles" -> {
                    ProfileTab(navController, profiles, profileViewModel, mainActivity)
                }

                "contacts" -> {
                    //profileViewModel.getSelectedProfile()
                    ContactTab(navController, profiles, contacts, contactViewModel, mainActivity)
                }

                "favorites" -> {
                    //profileViewModel.getSelectedProfile()
                    FavoriteTab(navController, profiles, favoriteContacts, favoriteViewModel, mainActivity, contactViewModel)
                }

                else -> {
                    // Handle other cases or leave it empty
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactTabsPreview() {
    ContactsAppTheme { // Use the ContactsTheme here
        MainActivity()
        //ContactTabs()
    }
}




