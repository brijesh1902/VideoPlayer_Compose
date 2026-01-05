package com.brizz.videoplayer.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.brizz.videoplayer.R
import com.brizz.videoplayer.models.VideoFolder
import com.brizz.videoplayer.ui.screens.PlayerActivity.Companion.isVLCPlayer
import com.brizz.videoplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

private const val TAG = "MainActivity"
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.setPermissionState(isGranted)
        }

        setContent {
            val navController = rememberNavController()
            val permissionGranted by viewModel.permissionGranted
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                   /* NavHost(navController = navController, startDestination = SPLASH_SCREEN_ROUTE) {
                        composable(SPLASH_SCREEN_ROUTE) {
                            SplashScreen(navController)
                        }
                        composable(MAIN_SCREEN_ROUTE) {
                            MainScreen(viewModel, permissionGranted, permissionLauncher)
                        }
                    }*/
                    MainScreen(viewModel, permissionGranted, permissionLauncher)
                }
            }

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, permissionGranted: Boolean, permissionLauncher: ActivityResultLauncher<String>) {
    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar (
            title = { Text("Home") },
            actions = {
                /*Switch(
                    checked = isVLCPlayer,
                    onCheckedChange = {
                        isVLCPlayer = it
                    },
                    thumbContent = if (isVLCPlayer) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    }
                )*/
            }
        )

        val videoFolders by viewModel.videoFolderList
        val isLoading by viewModel.isLoading
        val context = LocalContext.current

        if (permissionGranted) {
            VideoFolderListScreen(
                videoFolders = videoFolders,
                isLoading = isLoading,
                onFolderClick = { folderName ->
                    // Navigate to a new screen or update the current one to show folderVideos
                    Intent(context, VideoActivity::class.java).apply {
                        this.putExtra("folderName", folderName)
                        context.startActivity(this)
                    }
                }
            )
        } else {
            PermissionRequestScreen {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }
}

@Composable
fun VideoFolderListScreen(
    videoFolders: List<VideoFolder>,
    isLoading: Boolean,
    onFolderClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(0.dp, 8.dp), state = listState) {
            itemsIndexed(videoFolders) { index, folder ->
                VideoFolderItem(folder, onFolderClick)
                if (index < videoFolders.size - 1) HorizontalDivider()
            }
            /*if (isLoading && videoFolders.isEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
            }*/

        }
        if (isLoading && videoFolders.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        if (videoFolders.isEmpty() && !isLoading) {
            Text(
                text = "No video folders found.",
                modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VideoFolderItem(folder: VideoFolder, onFolderClick: (String) -> Unit) {
    val context = LocalContext.current
    val folderIconTintColor = remember {
        Color(ContextCompat.getColor(context, R.color.light_blue))
    }
    Box(modifier = Modifier.fillMaxSize().clickable { onFolderClick(folder.name) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // You can add a folder icon here
            Icon(imageVector = Icons.Filled.Folder, contentDescription = "Folder Icon", tint = folderIconTintColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = folder.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "${folder.videoCount} Videos", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Permission required to access video files.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Request Permission")
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
