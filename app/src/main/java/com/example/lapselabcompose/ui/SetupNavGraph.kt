package com.example.lapselabcompose.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Serializable
data object SplashScreen

@Serializable
data object GalleryScreen


@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController, startDestination = SplashScreen
    ) {
        composable<SplashScreen> {
            SplashScreen(navController)
        }
        composable<GalleryScreen> {
            Gallery(navController)
        }
    }
}