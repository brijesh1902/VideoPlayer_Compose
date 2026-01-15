package com.brizz.videoplayer.di

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brizz.videoplayer.ui.screens.FolderScreen
import com.brizz.videoplayer.ui.screens.PermissionScreen
import com.brizz.videoplayer.ui.screens.VideoListMainScreen
import com.brizz.videoplayer.ui.screens.VideoPlayerScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Permission.route
    ) {

        composable(Screen.Permission.route) {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.FolderScreen.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.FolderScreen.route) {
            FolderScreen(
                onFolderClick = { folderPath ->
                    navController.navigate(
                        Screen.VideoListScreen.createRoute(folderPath)
                    )
                }
            )
        }

        composable(
            route = Screen.VideoListScreen.route,
            arguments = listOf(
                navArgument("folderPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val folderPath = backStackEntry.arguments?.getString("folderPath")!!

            VideoListMainScreen(
                folderPath = folderPath,
                onBack = {
                    navController.popBackStack()
                },
                onPlay = {
                    navController.navigate(Screen.VideoPlayerScreen.createRoute(it))
                })
        }

        composable(
            route = Screen.VideoPlayerScreen.route,
            arguments = listOf(
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath")!!
            VideoPlayerScreen(
                filePath
            ) {
                navController.popBackStack()
            }
        }
    }
}
