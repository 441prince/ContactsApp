package com.prince.contactsapp.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.prince.contactsapp.R
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.viewmodel.ContactViewModel
import com.prince.contactsapp.viewmodel.FavoriteViewModel

@Composable
fun FavoriteTab(
    navController: NavController,
    profiles: List<Profile>,
    favoriteContacts: List<Contact>,
    favoriteViewModel: FavoriteViewModel,
    itemClickListener: ItemClickListener,
    contactViewModel: ContactViewModel
) {
    Log.d("FT FavoriteTab" ,"FavoriteTab() Called")
    FavoriteContactList(navController, profiles, favoriteContacts, favoriteViewModel, itemClickListener)

}

@Composable
fun FavoriteContactList(
    navController: NavController,
    profiles: List<Profile>,
    favoriteContacts: List<Contact>,
    favoriteViewModel: FavoriteViewModel,
    itemClickListener: ItemClickListener,
) {
    if (favoriteContacts.isEmpty()) {
        Log.d("favoriteContactsList", "The list is empty.")
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
        LazyVerticalGrid(
            modifier = Modifier
                .height(heightInDp)
                .align(Alignment.TopStart),
            //.padding(16.dp),
            verticalArrangement = Arrangement.Top,
            columns = GridCells.Fixed(2),
            userScrollEnabled = true
        ) {
            itemsIndexed(favoriteContacts) { index, favoriteContact ->
                FavoriteContactCard(favoriteContact = favoriteContact, itemClickListener = itemClickListener,favoriteViewModel)
            }
        }
    }
}

@Composable
fun FavoriteContactCard(favoriteContact: Contact, itemClickListener: ItemClickListener, favoriteViewModel: FavoriteViewModel) {

    // State to track whether a long-press is in progress
    var isLongPressActive by remember { mutableStateOf(false) }
    val context = LocalContext.current
    /*if (profile.isSelected) {
        containerColor = Color(216, 221, 223, 100)

    } else {
        containerColor = Color.Transparent
    }*/
    val imageUri = favoriteContact.imageUri

    Log.d("ProfileCard before", "Image URI: $imageUri")
    val imagePainter = if (imageUri != null  && imageUri != "null") {
        Log.d("CardView if", " ${favoriteContact.imageUri} , ${favoriteContact.name}")
        rememberAsyncImagePainter(model = imageUri) // Load the image from URI

    } else {
        // Use a default image model (not null)
        Log.d("CardView else", " ${favoriteContact.imageUri} , ${favoriteContact.name}")
        rememberAsyncImagePainter(model = R.drawable.contactblack)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .clickable {
                itemClickListener.onContactClick(favoriteContact, context)
            }
            .padding(1.dp)
            .size(200.dp, 200.dp),
        colors = CardColors(
            containerColor = Color.White,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(modifier = Modifier.width(30.dp))

                // Display the image
                Image(
                    painter = imagePainter,
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Top)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp, // Border width
                            color = MaterialTheme.colorScheme.primary, // Border color
                            shape = CircleShape
                        )
                        .background(MaterialTheme.colorScheme.primary)
                )

                Image(
                    painter = painterResource(id = if (favoriteContact.isFavorite) R.drawable.filledheart else R.drawable.emptyheart), //R.drawable.filledheart),
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Top)
                        .background(Color.Transparent, CircleShape)
                        .padding(5.dp)
                        .clickable {
                            //itemClickListener.onFavoriteButtonClick()
                            Log.d("FT FavoriteTab", "FavoriteButton onclick Called")
                            favoriteContact.isFavorite = !favoriteContact.isFavorite

                            //Toast.makeText(context, "$isFavorite, ${contact.isFavorite}", Toast.LENGTH_SHORT).show()
                            //favoriteViewModel.updateContactAndNotify(favoriteContact)
                            //favoriteViewModel.getUpdatedFavoriteContacts(favoriteContact.profileId)
                            itemClickListener.onFavoriteContactFavIconClick(
                                favoriteContact,
                                context = context
                            )

                            //painter = painterResource(id = R.drawable.filledheart)
                        }
                )
            }

            Text(
                text = favoriteContact.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp),
                textAlign = TextAlign.Center,
            )

            Text(
                text = favoriteContact.phoneNumber,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}