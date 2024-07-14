package com.example.lapselabcompose.ui.camera

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
object PhotoDestination

@Composable
fun PhotoRoute() {
    PhotoScreen()
}

@Composable
fun PhotoScreen(

) {
    Column {

        Text(text = "Photo SCREEN")
    }
}

