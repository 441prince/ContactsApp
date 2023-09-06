package com.prince.contactsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.prince.contactsapp.ui.theme.ContactsAppTheme
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.ConstraintLayout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactsAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactTabs()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactTabs() {

    // Create a list of tab titles
    val tabTitles = listOf("Profiles", "Contacts", "Favorites")

    // Initialize a custom state to manage the current page
    var currentPage by remember { mutableStateOf(0) }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        // Create a TopAppBar with the app name
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
        val tabRowGuideline = createGuidelineFromTop(0.08f) // Adjust the top guideline as needed

        // Create a TabRow with the tab titles
        TabRow(
            selectedTabIndex = currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    color = Color.White,
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentPage])
                )
            },
            tabs = {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(text = title) },
                        selected = currentPage == index,
                        onClick = {
                            // Change the current page when a tab is clicked
                            currentPage = index
                        }
                    )
                }
            },
            //backgroundColor = Color.Transparent, // Make the TabRow background transparent
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .constrainAs(tabRow) {
                    top.linkTo(tabRowGuideline) // Place TabRow below the TopAppBar
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        // Content for the selected tab
        Column(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(content) {
                    top.linkTo(tabRow.bottom) // Place content below the TabRow
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            when (currentPage) {
                0 -> ProfilesTab()
                1 -> ContactsTab()
                2 -> FavoritesTab()
                else -> throw IllegalArgumentException("Invalid page: $currentPage")
            }
        }
    }
}

@Composable
fun ProfilesTab() {
    Text("Profiles Tab Content")
}

@Composable
fun ContactsTab() {
    Text("Contacts Tab Content")
}

@Composable
fun FavoritesTab() {
    Text("Favorites Tab Content")
}

@Preview(showBackground = true)
@Composable
fun ContactTabsPreview() {
    ContactsAppTheme {
        ContactTabs()
    }
}