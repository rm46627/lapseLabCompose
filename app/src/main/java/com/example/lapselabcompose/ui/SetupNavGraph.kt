package com.example.lapselabcompose.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.lapselabcompose.PermissionViewModel
import com.example.lapselabcompose.ui.camera.CameraDestination
import com.example.lapselabcompose.ui.camera.CameraRoute
import com.example.lapselabcompose.ui.camera.PhotoDestination
import com.example.lapselabcompose.ui.camera.PhotoRoute
import com.example.lapselabcompose.ui.gallery.GalleryDestination
import com.example.lapselabcompose.ui.gallery.GalleryRoute
import com.example.lapselabcompose.ui.setup.AlbumSetupDestination
import com.example.lapselabcompose.ui.setup.AlbumSetupRoute
import com.example.lapselabcompose.ui.setup.FirstPhotoDestination
import com.example.lapselabcompose.ui.setup.FirstPhotoRoute
import kotlinx.serialization.Serializable

@Serializable
object TakingPhotoGraph

@Serializable
object CreatingAlbumGraph

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
                GalleryRoute(
                    onAlbumClick = ::inGalleryOnAlbumClick,
                    onCreateClick = ::inGalleryOnCreateClick
                )
            }
            creatingAlbumGraph(navController, permissionsResultLaunch, permissionViewModel)
            takingPhotoGraph(navController)
        }
    }

    private fun NavGraphBuilder.creatingAlbumGraph(
        navController: NavHostController,
        permissionsResultLaunch: () -> Unit,
        permissionViewModel: PermissionViewModel
    ) {
        navigation<CreatingAlbumGraph>(startDestination = AlbumSetupDestination) {
            composable<AlbumSetupDestination> {
                AlbumSetupRoute(navController)
            }
            composable<FirstPhotoDestination> {
                val args = it.toRoute<FirstPhotoDestination>()
                FirstPhotoRoute(
                    navController,
                    permissionsResultLaunch,
                    permissionViewModel,
                    args.albumName
                )
            }
        }
    }

    private fun NavGraphBuilder.takingPhotoGraph(navController: NavHostController) {
        navigation<TakingPhotoGraph>(startDestination = CameraDestination()) {
            composable<CameraDestination> {
                val args = it.toRoute<CameraDestination>()
                CameraRoute(navController, args.albumName)
            }
            composable<PhotoDestination> {
                PhotoRoute(navController)
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
}