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
import com.example.lapselabcompose.ui.details.DetailsDestination
import com.example.lapselabcompose.ui.details.DetailsRoute
import com.example.lapselabcompose.ui.gallery.GalleryDestination
import com.example.lapselabcompose.ui.gallery.GalleryRoute
import com.example.lapselabcompose.ui.lab.LabDestination
import com.example.lapselabcompose.ui.lab.LabRoute
import com.example.lapselabcompose.ui.setup.SetupAlbumDestination
import com.example.lapselabcompose.ui.setup.SetupAlbumRoute
import com.example.lapselabcompose.ui.setup.SetupPhotoDestination
import com.example.lapselabcompose.ui.setup.SetupPhotoRoute
import kotlinx.serialization.Serializable

@Serializable
object CameraGraph

@Serializable
object SetupGraph

@Serializable
object DetailsGraph

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
            composable<GalleryDestination>{
                GalleryRoute(navController)
            }
            setupGraph(navController, permissionsResultLaunch, permissionViewModel)
            cameraGraph(navController)
            detailsGraph(navController)
        }
    }

    private fun NavGraphBuilder.setupGraph(
        navController: NavHostController,
        permissionsResultLaunch: () -> Unit,
        permissionViewModel: PermissionViewModel
    ) {
        navigation<SetupGraph>(startDestination = SetupAlbumDestination) {
            composable<SetupAlbumDestination> { backStackEntry ->
                SetupAlbumRoute(backStackEntry, navController)
            }
            composable<SetupPhotoDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<SetupPhotoDestination>()
                SetupPhotoRoute(
                    backStackEntry,
                    navController,
                    permissionsResultLaunch,
                    permissionViewModel,
                    args.albumName
                )
            }
        }
    }

    private fun NavGraphBuilder.cameraGraph(navController: NavHostController) {
        navigation<CameraGraph>(startDestination = CameraDestination()) {
            composable<CameraDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<CameraDestination>()
                CameraRoute(backStackEntry, navController, args.albumName, args.navigatedFromAlbumDetails)
            }
            composable<PhotoDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<PhotoDestination>()
                PhotoRoute(backStackEntry, navController, args.navigatedFromAlbumDetails)
            }
        }
    }

    private fun NavGraphBuilder.detailsGraph(navController: NavHostController) {
        navigation<DetailsGraph>(startDestination = DetailsDestination()) {
            composable<DetailsDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<DetailsDestination>()
                DetailsRoute(backStackEntry, navController, args.albumName)
            }
            composable<LabDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<LabDestination>()
                LabRoute(args.albumName ?: throw IllegalArgumentException())
            }
        }
    }
}