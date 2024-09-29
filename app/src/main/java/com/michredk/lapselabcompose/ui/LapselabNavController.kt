package com.michredk.lapselabcompose.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.michredk.lapselabcompose.ui.camera.CameraDestination
import com.michredk.lapselabcompose.ui.camera.CameraRoute
import com.michredk.lapselabcompose.ui.camera.PhotoPreviewDestination
import com.michredk.lapselabcompose.ui.camera.PhotoPreviewRoute
import com.michredk.lapselabcompose.ui.details.DetailsDestination
import com.michredk.lapselabcompose.ui.details.DetailsRoute
import com.michredk.lapselabcompose.ui.details.PhotoBrowserDestination
import com.michredk.lapselabcompose.ui.details.PhotoBrowserRoute
import com.michredk.lapselabcompose.ui.gallery.GalleryDestination
import com.michredk.lapselabcompose.ui.gallery.GalleryRoute
import com.michredk.lapselabcompose.ui.details.LabDestination
import com.michredk.lapselabcompose.ui.details.LabRoute
import com.michredk.lapselabcompose.ui.startup.OnBoardingDestination
import com.michredk.lapselabcompose.ui.startup.OnBoardingRoute
import com.michredk.lapselabcompose.ui.startup.SplashScreenDestination
import com.michredk.lapselabcompose.ui.startup.SplashScreenRoute
import com.michredk.lapselabcompose.ui.setup.SetupAlbumDestination
import com.michredk.lapselabcompose.ui.setup.SetupAlbumRoute
import com.michredk.lapselabcompose.ui.setup.SetupPhotoDestination
import com.michredk.lapselabcompose.ui.setup.SetupPhotoRoute
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object CameraGraph

@Serializable
object SetupGraph

@Serializable
object DetailsGraph

@Serializable
object PhotosGraph

class LapselabNavController(
    private val navController: NavHostController,
) {

    @SuppressLint("RestrictedApi")
    @Composable
    fun SetupNavGraph(
        modifier: Modifier,
        permissionsResultLaunch: () -> Unit,
        permissionViewModel: PermissionViewModel,
        showInterstitialAd: () -> Unit
    ) {

        navController.addOnDestinationChangedListener { controller, dest, _ ->
            val routes = controller
                .currentBackStack.value
                .map { it.destination.route }
                .joinToString(",\n\t")

            Log.d("BackStackLog", "BackStack: $routes\n\t${dest.route}")
        }
        NavHost(
            modifier = Modifier, navController = navController,
            startDestination = SplashScreenDestination
        ) {
            composable<SplashScreenDestination> {
                SplashScreenRoute(navController)
            }
            composable<OnBoardingDestination> {
                OnBoardingRoute(navController)
            }
            composable<GalleryDestination> {
                GalleryRoute(navController)
            }
            setupGraph(navController, permissionsResultLaunch, permissionViewModel)
            cameraGraph(navController)
            detailsGraph(
                navController,
                permissionsResultLaunch,
                permissionViewModel,
                showInterstitialAd
            )
            photosGraph(navController)
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
            cameraGraph(navController)
        }
    }

    private fun NavGraphBuilder.cameraGraph(navController: NavHostController) {
        navigation<CameraGraph>(startDestination = CameraDestination()) {
            composable<CameraDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<CameraDestination>()
                CameraRoute(
                    backStackEntry,
                    navController,
                    args.albumName,
                    args.navigatedFromAlbumDetails
                )
            }
            composable<PhotoPreviewDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<PhotoPreviewDestination>()
                PhotoPreviewRoute(backStackEntry, navController, args.navigatedFromAlbumDetails)
            }
        }
    }

    private fun NavGraphBuilder.detailsGraph(
        navController: NavHostController,
        permissionsResultLaunch: () -> Unit,
        permissionViewModel: PermissionViewModel,
        showInterstialAd: () -> Unit
    ) {
        navigation<DetailsGraph>(startDestination = DetailsDestination()) {
            composable<DetailsDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<DetailsDestination>()

                DetailsRoute(
                    backStackEntry, navController, args.albumName,
                    permissionsResultLaunch,
                    permissionViewModel
                )
            }
            composable<LabDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<LabDestination>()
                LabRoute(backStackEntry, navController, args.albumName, showInterstialAd)
            }
        }
    }

    private fun NavGraphBuilder.photosGraph(navController: NavHostController) {
        navigation<PhotosGraph>(startDestination = PhotoBrowserDestination()) {
            composable<PhotoBrowserDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<PhotoBrowserDestination>()
                PhotoBrowserRoute(backStackEntry, navController, args.index)
            }
        }
    }

}