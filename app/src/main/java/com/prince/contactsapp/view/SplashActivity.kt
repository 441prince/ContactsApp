package com.prince.contactsapp.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.window.SplashScreen
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.prince.contactsapp.view.ui.theme.ContactsAppTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prince.contactsapp.R
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ContactsAppTheme {
                // Use the SplashScreen composable
                SplashScreen(modifier = Modifier.fillMaxSize()) {
                    // Handle navigation after the splash screen timeout
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onTimeout: () -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current.density

    // Create a coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Use the rememberUpdatedState to prevent recomposition when changing the timeout value
    val timeout by rememberUpdatedState(3000L)

    DisposableEffect(view) {
        // Set up a coroutine to navigate after the timeout
        coroutineScope.launch {
            delay(timeout)
            onTimeout()
        }

        onDispose {
            // Dispose of the coroutine scope when the composable is removed from the view hierarchy
            coroutineScope.coroutineContext.cancel()
        }
    }

    // Composable content for the splash screen
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Replace "R.drawable.contacts" with the actual resource ID for your image
        Image(
            painter = painterResource(id = R.drawable.contacts),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp, 100.dp) // Set the desired size of the image
                .scale(1f + density * 0.2f), // Scale the image if needed
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun SplashScreenPreview() {
    ContactsAppTheme {
        SplashScreen(modifier = Modifier.fillMaxSize()) {
            // This is just a preview, so you can leave the action empty
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreviewWrapper() {
    SplashScreenPreview()
}