package com.example.lapselabcompose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.compose.LapseLabComposeTheme
import com.example.compose.primaryLight

@Composable
fun Gallery(navController: NavController) {
    Scaffold {
        Box(modifier = Modifier.padding(it).fillMaxSize().background(primaryLight)) {

        }
    }
}

@Preview
@Composable
fun PreviewGallery() {
    LapseLabComposeTheme {
        Gallery(navController = rememberNavController())
    }
}