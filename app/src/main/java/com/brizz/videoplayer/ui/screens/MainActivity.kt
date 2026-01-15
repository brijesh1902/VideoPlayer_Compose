package com.brizz.videoplayer.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.brizz.videoplayer.R
import com.brizz.videoplayer.di.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph()
                }
            }

        }
    }

}

const val SPLASH_SCREEN_ROUTE = "splash"
const val MAIN_SCREEN_ROUTE = "main"

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val tintColor = remember {
        Color(ContextCompat.getColor(context, R.color.light_blue))
    }
    val appLogo = remember {
        Color(ContextCompat.getColor(context, R.color.light_blue))
    }
    LaunchedEffect(key1 = true) {
        delay(2000) // Simulate a 2-second delay
        navController.navigate(MAIN_SCREEN_ROUTE) {
            popUpTo(SPLASH_SCREEN_ROUTE) { inclusive = true } // Prevent going back to splash screen
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // You can customize the background color
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow, // Replace with your app's logo icon
            contentDescription = "App Logo",
            tint = tintColor, // Customize the icon color
            modifier = Modifier.fillMaxSize(0.3f) // Adjust the size of the icon
        )
        Text(
            text = "Video Player", // Replace with your app's name
            style = MaterialTheme.typography.headlineLarge,
            color = tintColor,
            fontSize = 32.sp
        )
    }
}
