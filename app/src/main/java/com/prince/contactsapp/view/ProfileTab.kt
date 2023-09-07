package com.prince.contactsapp.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat.startActivity
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.prince.contactsapp.R
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.*
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.platform.LocalDensity
import kotlin.time.milliseconds

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
    val maxProfilesAllowed = 3 // Set the maximum number of profiles allowed
    val context = LocalContext.current
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
            }
        }


        FloatingActionButton(
            onClick = {
                if (profiles.size < maxProfilesAllowed) {
                    // Open the new activity with extras
                    val intent = Intent(context, AddViewEditProfileActivity::class.java)
                    /*// Put your extras here
                    intent.putExtra("key1", "value1")
                    intent.putExtra("key2", "value2")*/
                    // Add more extras if needed
                    startActivity(context, intent, null)
                    //(context as Activity).finish()
                } else {
                    // Show a toast message if there are too many profiles
                    Toast.makeText(context,
                        "Maximum of three profiles allowed.", Toast.LENGTH_SHORT).show()
                }
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

    // State to track whether a long-press is in progress
    var isLongPressActive by remember { mutableStateOf(false) }

    var containerColor: Color
    val context = LocalContext.current
    if (profile.isSelected) {
        containerColor = Color(216, 221, 223, 100)

    } else {
        containerColor = Color.Transparent
    }
    val imageUri = profile.imageUri

    Log.d("ProfileCard before", "Image URI: $imageUri")
    val imagePainter = if (imageUri != null  && imageUri != "null") {
        Log.d("CardView if", " ${profile.imageUri} , ${profile.name}")
        rememberAsyncImagePainter(model = imageUri) // Load the image from URI

    } else {
        // Use a default image model (not null)
        Log.d("CardView else", " ${profile.imageUri} , ${profile.name}")
        rememberAsyncImagePainter(model = R.drawable.contactblack)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .clickable {
                // Handle regular click
                if (!isLongPressActive) {
                    itemClickListener.onProfileClick(profile, context)
                }
            }
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
                        color = MaterialTheme.colorScheme.primary, // Border color
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