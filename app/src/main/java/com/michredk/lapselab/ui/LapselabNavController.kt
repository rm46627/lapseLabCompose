package com.michredk.lapselab.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.michredk.lapselab.ui.camera.CameraDestination
import com.michredk.lapselab.ui.camera.CameraRoute
import com.michredk.lapselab.ui.camera.PhotoPreviewDestination
import com.michredk.lapselab.ui.camera.PhotoPreviewRoute
import com.michredk.lapselab.ui.details.DetailsDestination
import com.michredk.lapselab.ui.details.DetailsRoute
import com.michredk.lapselab.ui.details.PhotoBrowserDestination
import com.michredk.lapselab.ui.details.PhotoBrowserRoute
import com.michredk.lapselab.ui.gallery.GalleryDestination
import com.michredk.lapselab.ui.gallery.GalleryRoute
import com.michredk.lapselab.ui.details.LabDestination
import com.michredk.lapselab.ui.details.LabRoute
import com.michredk.lapselab.ui.startup.OnBoardingDestination
import com.michredk.lapselab.ui.startup.OnBoardingRoute
import com.michredk.lapselab.ui.startup.SplashScreenDestination
import com.michredk.lapselab.ui.startup.SplashScreenRoute
import com.michredk.lapselab.ui.setup.SetupAlbumDestination
import com.michredk.lapselab.ui.setup.SetupAlbumRoute
import com.michredk.lapselab.ui.setup.SetupPhotoDestination
import com.michredk.lapselab.ui.setup.SetupPhotoRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    @SuppressLint("RestrictedApi", "FlowOperatorInvokedInComposition")
    @Composable
    fun SetupNavGraph(
        modifier: Modifier,
        permissionsResultLaunch: () -> Unit,
        permissionViewModel: PermissionViewModel,
        showInterstitialAd: () -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val backStack = remember {
            navController.currentBackStack
                .map { stackEntries ->
                    stackEntries.map { entry -> entry.destination.route }
                }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList()
                )
        }
        val bs by backStack.collectAsStateWithLifecycle()

        LaunchedEffect(bs) {
            Log.d("BackStackLog", "BackStack changed: $bs")
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
                SetupAlbumRoute(
                    backStackEntry,
                    navController,
                    permissionsResultLaunch,
                    permissionViewModel,
                )
            }
            composable<SetupPhotoDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<SetupPhotoDestination>()
                SetupPhotoRoute(
                    backStackEntry,
                    navController,
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