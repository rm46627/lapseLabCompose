package com.example.lapselabcompose.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme

@Composable
fun SplashScreen() {

}

@Preview
@Composable
fun PreviewSplash(){
    LapseLabComposeTheme {
        val navController = rememberNavController()
        SetupNavGraph(navController = navController)
    }
}