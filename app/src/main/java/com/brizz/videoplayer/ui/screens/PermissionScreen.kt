package com.brizz.videoplayer.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brizz.videoplayer.viewmodel.MainViewModel

@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {

    val permissionGranted by viewModel.permissionGranted

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            viewModel.setPermissionState(granted)
            if (granted) onPermissionGranted()
        }

    LaunchedEffect(Unit) {
        requestPermission(permissionLauncher)
    }

    if (!permissionGranted) {
        PermissionRequestScreen(permissionGranted) {
            requestPermission(permissionLauncher)
        }
    }
}

private fun requestPermission(
    launcher: ManagedActivityResultLauncher<String, Boolean>
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

@Composable
fun PermissionRequestScreen(permissionGranted: Boolean, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = !permissionGranted
        ) {
            Text("Permission required to access video files.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text("Request Permission")
            }
        }
    }
}


