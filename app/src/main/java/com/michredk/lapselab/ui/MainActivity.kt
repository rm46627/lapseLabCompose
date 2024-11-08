package com.michredk.lapselab.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.rememberNavController
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform.showPrivacyOptionsForm
//import com.google.firebase.analytics.ktx.analytics
//import com.google.firebase.ktx.Firebase
import com.michredk.lapselab.ui.common.ObserveAsEvents
import com.michredk.lapselab.TAG
import com.michredk.lapselab.services.SnackbarController
import com.michredk.lapselab.ui.common.CameraPermissionTextProvider
import com.michredk.lapselab.ui.common.PermissionDialog
import com.michredk.lapselab.ui.common.PostNotificationsPermissionTextProvider
import com.michredk.lapselab.ui.common.ReadExternalStoragePermissionTextProvider
import com.michredk.lapselab.ui.common.WriteExternalStoragePermissionTextProvider
import com.michredk.lapselab.ui.theme.LapseLabComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: add longer AD if album has more than 10 photos

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        this.actionBar?.hide()

        CoroutineScope(Dispatchers.IO).launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf("545D4AC067871249CD54F65223A886E5")).build()
            )
            runOnUiThread {
                loadInterstitialAd()
            }
        }

        val permissionViewModel by viewModels<PermissionViewModel>()
        val dialogQueue = permissionViewModel.visiblePermissionDialogQueue
        Log.d(TAG, "permissions saved to val")
        val permissionsToRequest = permissionViewModel.photosPermissionsToRequest

        permissionViewModel.setAllPermissionsGranted(permissionsToRequest.fold(true) { acc, permission ->
            Log.d(TAG, "perm: $permission")
            acc && isPermissionGranted(this, permission)
        })

        setContent {
            LapseLabComposeTheme {
                val multiplePermissionResultLauncher =
                    managedActivityResultLauncher(permissionViewModel)
                val snackbarHostState = remember {
                    SnackbarHostState()
                }
                val scope = rememberCoroutineScope()
                ObserveAsEvents(flow = SnackbarController.events, snackbarHostState) { event ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionObj?.name,
                            duration = event.duration
                        )
                        if(result == SnackbarResult.ActionPerformed) {
                            event.actionObj?.action?.invoke()
                        }
                    }
                }
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LapselabNavController(rememberNavController()).SetupNavGraph(
                        modifier = Modifier.padding(innerPadding),
                        permissionsResultLaunch = {
                            multiplePermissionResultLauncher.launch(permissionsToRequest)
                        },
                        permissionViewModel = permissionViewModel,
                        showInterstitialAd = ::showInterstitialAd
                    )
                    DisplayPermissionDialogs(
                        dialogQueue, permissionViewModel, multiplePermissionResultLauncher
                    )
                }
            }
        }
    }

    @Composable
    private fun managedActivityResultLauncher(permissionViewModel: PermissionViewModel): ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {
        val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { perms ->
                permissionViewModel.photosPermissionsToRequest.forEach { permission ->
                    permissionViewModel.onPermissionResult(
                        permission = permission, isGranted = perms[permission] == true
                    )
                }
            })
        return multiplePermissionResultLauncher
    }

    @Composable
    private fun DisplayPermissionDialogs(
        dialogQueue: SnapshotStateList<String>,
        permissionViewModel: PermissionViewModel,
        multiplePermissionResultLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
    ) {
        dialogQueue.reversed().forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.CAMERA -> {
                        CameraPermissionTextProvider()
                    }

                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        ReadExternalStoragePermissionTextProvider()
                    }

                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        WriteExternalStoragePermissionTextProvider()
                    }

                    Manifest.permission.POST_NOTIFICATIONS -> {
                        PostNotificationsPermissionTextProvider()
                    }

                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(permission),
                onDismiss = permissionViewModel::dismissDialog,
                onOkClick = {
                    permissionViewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = ::openAppSettings
            )
        }
    }

    private fun loadInterstitialAd() {
        Log.d(TAG,"LOADING ADDDDDD")
        if (adIsLoading || interstitialAd != null) {
            return
        }
        adIsLoading = true
        InterstitialAd.load(
            this,
//            "ca-app-pub-3940256099942544/1033173712", real adUnitId: ca-app-pub-1091895857582029/1209168792
            "ca-app-pub-3940256099942544/1033173712",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    adIsLoading = false
                    val error =
                        "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                    Log.d(TAG, "onAdFailedToLoad: ${adError.message}, $error")
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    interstitialAd = ad
                    adIsLoading = false
                }
            }
        )
    }

    private fun showInterstitialAd() {
        if (interstitialAd == null) {
            loadInterstitialAd()
        }
        Log.d(TAG, "ShowInterstitalAd running on thread: ${Thread.currentThread().name}")
        interstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    interstitialAd = null
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d(TAG, "Ad failed to show.")
                    interstitialAd = null
                    loadInterstitialAd()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                    loadInterstitialAd()
                }
            }
        interstitialAd?.show(this)

    }
}

private fun isPermissionGranted(context: Context, permission: String) =
    (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)

private fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

@SuppressLint("InlinedApi")
@HiltViewModel
class PermissionViewModel @Inject constructor() : ViewModel() {

    var photosPermissionsToRequest =
        arrayOf(
            Manifest.permission.CAMERA
        )

    private val permissionsMap = mutableMapOf(
        Pair(Manifest.permission.CAMERA, false)
    )
    private val _allPermissionsGranted = MutableStateFlow(false)
    val allPermissionsGranted = _allPermissionsGranted.asStateFlow()
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    init {
        if (Build.VERSION.SDK_INT <= 28) {
            photosPermissionsToRequest = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            permissionsMap[Manifest.permission.READ_EXTERNAL_STORAGE] = false
            permissionsMap[Manifest.permission.WRITE_EXTERNAL_STORAGE] = false
        } else if (Build.VERSION.SDK_INT >= 33){
            photosPermissionsToRequest = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            )
            permissionsMap[Manifest.permission.POST_NOTIFICATIONS] = false
        }
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeAt(0)
    }

    fun onPermissionResult(
        permission: String, isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        } else {
            permissionsMap[permission] = isGranted
            _allPermissionsGranted.value = permissionsMap.values.all { it }
        }
    }

    fun setAllPermissionsGranted(value: Boolean) = run { _allPermissionsGranted.value = value }
}