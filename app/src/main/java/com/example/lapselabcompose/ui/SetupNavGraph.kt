package com.example.lapselabcompose.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.lapselabcompose.PermissionViewModel
import com.example.lapselabcompose.ui.camera.CameraDestination
import com.example.lapselabcompose.ui.camera.CameraRoute
import com.example.lapselabcompose.ui.camera.CameraScreen
import com.example.lapselabcompose.ui.camera.PhotoDestination
import com.example.lapselabcompose.ui.camera.PhotoRoute
import com.example.lapselabcompose.ui.gallery.GalleryDestination
import com.example.lapselabcompose.ui.gallery.GalleryRoute
import com.example.lapselabcompose.ui.setup.AddFirstPhoto
import com.example.lapselabcompose.ui.setup.AlbumSetupDestination
import com.example.lapselabcompose.ui.setup.AlbumSetupRoute
import com.example.lapselabcompose.ui.setup.AlbumSetupScreen
import com.example.lapselabcompose.ui.setup.FirstPhotoDestination
import com.example.lapselabcompose.ui.setup.FirstPhotoRoute
import kotlinx.serialization.Serializable

@Serializable
object TakingPhotoGraph

@Serializable
object AlbumDetailsGraph

class LapselabNavController(
    private val navController: NavHostController,
) {

    @Composable
    fun SetupNavGraph(
        permissionsResultLaunch: () -> Unit,
        permissionViewModel: PermissionViewModel
    ) {
        NavHost(
            navController = navController, startDestination = SplashScreenDestination
        ) {
            composable<SplashScreenDestination> {
                SplashScreen(navController)
            }
            composable<GalleryDestination> {
                GalleryRoute(onAlbumClick = ::inGalleryOnAlbumClick, onCreateClick = ::inGalleryOnCreateClick)
            }
            composable<AlbumSetupDestination> {
                AlbumSetupRoute(onNextButtonClicked = ::inAlbumCreationOnNextClick)
            }
            composable<FirstPhotoDestination> {
                FirstPhotoRoute(
                    permissionsResultLaunch,
                    permissionViewModel,
                    onFirstImagePreviewClick = {
                        navController.navigate(TakingPhotoGraph)
                    },
                    navigateToGallery = {
                        navController.navigate(GalleryDestination)
                    }
                )
            }
            takingPhotoGraph(navController)
        }
    }

    private fun NavGraphBuilder.takingPhotoGraph(
        navController: NavHostController
    ) {
        navigation<TakingPhotoGraph>(startDestination = CameraDestination) {
            composable<CameraDestination> {
                CameraRoute()
            }
            composable<PhotoDestination> {
                PhotoRoute()
            }
        }
    }

    fun NavGraphBuilder.albumDetailsGraph(navController: NavHostController) {
        navigation<AlbumDetailsGraph>(startDestination = AlbumDetailsDestination) {
            composable<AlbumDetailsDestination> {
                AlbumDetailsRoute(id)
            }
        }
    }

    private fun inGalleryOnCreateClick() {
        navController.navigate(AlbumSetupDestination)
    }

    private fun inGalleryOnAlbumClick(id: Int) {
        navController.navigate(AlbumDetailsDestination(id))
    }

    private fun inAlbumCreationOnFirstPhotoClick() {
        navController.navigate(CameraDestination)
    }

    private fun inAlbumCreationOnNextClick() {
        navController.navigate(FirstPhotoDestination)
    }
}

