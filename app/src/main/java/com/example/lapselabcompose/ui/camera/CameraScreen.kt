package com.example.lapselabcompose.ui.camera

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
object CameraDestination

@Composable
fun CameraRoute() {
    CameraScreen()
}

@Composable
fun CameraScreen(

) {
    Column {

        Text(text = "CAMERA SCREEN")
        Text(text = "CAMERA SCREEN")
        Text(text = "CAMERA SCREEN")
        Text(text = "CAMERA SCREEN")
        Text(text = "CAMERA SCREEN")
        Text(text = "CAMERA SCREEN")
        Text(text = "CAMERA SCREEN")
    }
}

