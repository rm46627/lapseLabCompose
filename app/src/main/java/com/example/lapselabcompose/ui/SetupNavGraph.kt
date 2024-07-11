package com.example.lapselabcompose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.lapselabcompose.MainViewModel
import com.example.lapselabcompose.ui.camera.Camera
import com.example.lapselabcompose.ui.camera.Permissions
import com.example.lapselabcompose.ui.camera.PhotoView
import com.example.lapselabcompose.ui.setup.AddFirstPhoto
import com.example.lapselabcompose.ui.setup.AlbumSetup


@Serializable
object SplashScreen
@Serializable
object GalleryScreen
@Serializable
object AlbumCreationGraph
@Serializable
object AlbumSetupScreen
@Serializable
object AddFirstPhotoScreen

@Serializable
object TakingPhotoGraph
@Serializable
object PermissionsScreen
@Serializable
object CameraScreen
@Serializable
object PhotoViewScreen

@Serializable
object AlbumGraph

@Serializable
data class AlbumDetailsScreen(
    val albumId: Int
)

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    permissionsResultLaunch: () -> Unit,
    galleryViewModel: GalleryViewModel = hiltViewModel(),
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController, startDestination = SplashScreen
    ) {
        composable<SplashScreen> {
            SplashScreen(navController)
        }
        composable<GalleryScreen> {
            val albums by galleryViewModel.getAlbums.collectAsStateWithLifecycle(initialValue = emptyList())
            Gallery(
                navController, albums
            )
        }
        albumCreationGraph(navController, permissionsResultLaunch, mainViewModel)
        takingPhotoGraph(navController)
    }
}

fun NavGraphBuilder.albumCreationGraph(
    navController: NavHostController,
    permissionsResultLaunch: () -> Unit,
    mainViewModel: MainViewModel
) {
    navigation<AlbumCreationGraph>(startDestination = AlbumSetupScreen) {
        composable<AlbumSetupScreen> {
            AlbumSetup {
                navController.navigate(AddFirstPhotoScreen)
            }
        }
        composable<AddFirstPhotoScreen> {
            AddFirstPhoto(
                permissionsResultLaunch,
                mainViewModel,
                onFirstImagePreviewClick = {
                    navController.navigate(TakingPhotoGraph)
                },
                navigateToGallery = {
                    navController.navigate(GalleryScreen)
                }
            )
        }
    }
}

fun NavGraphBuilder.takingPhotoGraph(
    navController: NavHostController
) {
    navigation<TakingPhotoGraph>(startDestination = CameraScreen) {
//        composable<PermissionsScreen> {
//            Permissions(
//                navigateToCameraScreen = {
//                    navController.navigate(CameraScreen)
//                }
//            )
//        }
        composable<CameraScreen> {
            Camera(
                navigateToPermissionScreen = {
                    navController.navigate(PermissionsScreen)
                }
            )
        }
        composable<PhotoViewScreen> {
            PhotoView()
        }
    }
}

fun NavGraphBuilder.albumGraph(navController: NavHostController) {
    navigation<AlbumGraph>(startDestination = AlbumDetailsScreen) {
        composable<AlbumDetailsScreen> {
            AlbumDetails(navController)
        }
    }
}