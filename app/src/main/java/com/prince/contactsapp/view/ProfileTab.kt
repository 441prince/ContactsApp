package com.prince.contactsapp.view

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout

import com.prince.contactsapp.R
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.viewmodel.ProfileViewModel

@Composable
fun ProfileTab(
    navController: NavController,
    profiles: List<Profile>,
    profileViewModel: ProfileViewModel,
    itemClickListener: ItemClickListener
) {
    Log.d("ProfileTab", profiles.toString())
    ProfileList(navController, profiles, profileViewModel, itemClickListener)

}

fun getScreenHeightInDp(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    val screenHeightInPixels = displayMetrics.heightPixels
    val density = displayMetrics.density
    val screenHeightInDp = screenHeightInPixels / density
    return screenHeightInDp.toInt()
}

@Composable
fun ProfileList(
    navController: NavController,
    profiles: List<Profile>,
    profileViewModel: ProfileViewModel,
    itemClickListener: ItemClickListener
) {
    Log.d("ProfileList", profiles.toString())
    if (profiles.isEmpty()) {
        Log.d("ProfileList", "The list is empty.")
    }

    val ht: Int = getScreenHeightInDp(LocalContext.current)
    val heightInDp = ht.dp - 100.dp
    Log.d("Screen Height Prince", "Height = $ht.")
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .height(heightInDp)
                .align(Alignment.Center),
            //.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            userScrollEnabled = true
        ) {
            itemsIndexed(profiles) { index, profile ->
                ProfileCard(profile = profile, itemClickListener = itemClickListener)
                ProfileCard(profile = profile, itemClickListener = itemClickListener)
                ProfileCard(profile = profile, itemClickListener = itemClickListener)
                ProfileCard(profile = profile, itemClickListener = itemClickListener)
            }
        }

        // Floating action button should be here, inside the Box
        FloatingActionButton(
            onClick = {
                // Handle FloatingActionButton click
            },
            modifier = Modifier
                .padding(end = 20.dp, bottom = 40.dp)
                .align(Alignment.BottomEnd),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }
}

@Composable
fun ProfileCard(profile: Profile, itemClickListener: ItemClickListener) {

    var containerColor: Color
    if (profile.isSelected) {
        containerColor = Color(216, 221, 223, 100)

    } else {
        containerColor = Color.Transparent
    }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .size(200.dp, 200.dp),
        colors = CardColors(
            containerColor = containerColor,
            contentColor = Color.Black,
            disabledContainerColor = Color.DarkGray,
            disabledContentColor = Color.DarkGray
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(), // Occupy all available space
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Check if profile.imageUri is null
            val imageResourceId = if (profile.imageUri != null) {
                // Load the image from drawable resource based on profile.imageUri
                LocalContext.current.resources.getIdentifier(
                    profile.imageUri,
                    "drawable",
                    LocalContext.current.packageName
                )
            } else {
                // Use a default image resource ID when profile.imageUri is null
                R.drawable.contactblack // Replace with your default image resource ID
            }

            // Add a debug log to check the image resource ID
            Log.d("ProfileCard", "Image Resource ID: $imageResourceId")

            val imagePainter = painterResource(id = imageResourceId)

            // Display the image
            Image(
                painter = imagePainter,
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp, // Border width
                        color = Color.Blue, // Border color
                        shape = CircleShape
                    )
                    .background(MaterialTheme.colorScheme.primary)
            )

            Text(
                text = profile.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
