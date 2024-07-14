package com.example.lapselabcompose.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

@Serializable
data class AlbumDetailsDestination(val id: Int)

@Composable
fun AlbumDetailsRoute(id: Int) {
    AlbumDetails(id)
}

@Composable
fun AlbumDetails(id: Int) {

}
