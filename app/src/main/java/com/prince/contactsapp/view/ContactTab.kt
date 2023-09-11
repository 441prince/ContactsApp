package com.prince.contactsapp.view

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat.startActivity
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.prince.contactsapp.R
import com.prince.contactsapp.models.Contact
import com.prince.contactsapp.models.Profile
import com.prince.contactsapp.viewmodel.ContactViewModel

@Composable
fun ContactTab(
    navController: NavController,
    profiles: List<Profile>,
    contacts: List<Contact>,
    contactViewModel: ContactViewModel,
    itemClickListener: ItemClickListener
) {
    Log.d("ContactTab", profiles.toString())
    ContactList(navController, profiles, contacts, contactViewModel, itemClickListener)

}

@Composable
fun ContactList(
    navController: NavController,
    profiles: List<Profile>,
    contacts: List<Contact>,
    contactViewModel: ContactViewModel,
    itemClickListener: ItemClickListener
) {
    Log.d("ContactList", contacts.toString())
    if (contacts.isEmpty()) {
        Log.d("ContactList", "The list is empty.")
    }

    val ht: Int = getScreenHeightInDp(LocalContext.current)
    val heightInDp = ht.dp - 100.dp
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
                .align(Alignment.TopCenter),
            //.padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            userScrollEnabled = true
        ) {
            itemsIndexed(contacts) { index, contact ->
                ContactCard(contact, itemClickListener = itemClickListener, contactViewModel)
            }
        }

        FloatingActionButton(
            onClick = {
                val intent = Intent(context, AddNewContactActivity::class.java)
                /*// Put your extras here
                intent.putExtra("key1", "value1")
                intent.putExtra("key2", "value2")*/
                // Add more extras if needed
                startActivity(context, intent, null)
                //(context as Activity).finish()
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
fun ContactCard(contact: Contact, itemClickListener: ItemClickListener, contactViewModel: ContactViewModel) {

    var isFavorite by remember { mutableStateOf(contact.isFavorite) }

    val context = LocalContext.current
    val imageUri = contact.imageUri

    Log.d("ContactCard before", "Image URI: $imageUri")
    val imagePainter = if (imageUri != null && imageUri != "null") {
        Log.d("CardView if", " ${contact.imageUri} , ${contact.name}")
        rememberAsyncImagePainter(model = imageUri) // Load the image from URI

    } else {
        // Use a default image model (not null)
        Log.d("CardView else", " ${contact.imageUri} , ${contact.name}")
        rememberAsyncImagePainter(model = R.drawable.contactblack)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                itemClickListener.onContactClick(contact, context)
            }
            .padding(1.dp),
        colors = CardColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.DarkGray,
            disabledContentColor = Color.DarkGray
        )
        //elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CircleImageView
            Image(
                painter = imagePainter,   //rememberAsyncImagePainter(model = contact.imageUri ?: R.drawable.contactblack),
                contentDescription = "Contact Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, color = MaterialTheme.colorScheme.primary, CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // LinearLayout with TextViews
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(5.dp)
            ) {
                Text(
                    text = contact.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = contact.phoneNumber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            //Spacer(modifier = Modifier.weight(1f))

            // ImageButton
            Image(
                painter = painterResource(id = if (isFavorite) R.drawable.filledheart else R.drawable.emptyheart),
                contentDescription = "Favorite",
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent, CircleShape)
                    .padding(5.dp)
                    .clickable {
                        //itemClickListener.onFavoriteButtonClick()
                        contact.isFavorite = !contact.isFavorite
                        isFavorite = !isFavorite
                        //Toast.makeText(context, "$isFavorite, ${contact.isFavorite}", Toast.LENGTH_SHORT).show()
                        contactViewModel.updateContactAndNotify(contact)
                        //painter = painterResource(id = R.drawable.filledheart)
                    }
            )
        }
    }

    /*Card(
        modifier = Modifier
            .clickable {
                itemClickListener.onContactClick(contact, context)
            }
            .padding(10.dp)
            .fillMaxWidth(),
            //.size(200.dp, 200.dp),
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // Display the image
            Image(
                painter = imagePainter,
                contentDescription = "Contact Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp, // Border width
                        color = MaterialTheme.colorScheme.primary, // Border color
                        shape = CircleShape
                    )
                    .background(MaterialTheme.colorScheme.primary)
            )

            Text(
                text = contact.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )

            Text(
                text = contact.phoneNumber,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }*/
}