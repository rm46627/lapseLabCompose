package com.example.lapselabcompose.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Serializable
sealed class Screens {
    @Serializable
    data object SplashScreen : Screens()

    @Serializable
    data object Gallery : Screens()
}

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController, startDestination = Screens.SplashScreen
    ) {
        composable<Screens.SplashScreen> {
            SplashScreen(navController)
        }
        composable<Screens.Gallery> {
            Gallery()
        }
    }
}