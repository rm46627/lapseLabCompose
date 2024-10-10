package com.michredk.lapselabcompose.ui.setup


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.ui.PermissionViewModel
import com.michredk.lapselabcompose.ui.SetupGraph
import com.michredk.lapselabcompose.ui.camera.CameraDestination
import com.michredk.lapselabcompose.ui.common.BackHandlingDialog
import com.michredk.lapselabcompose.ui.common.DropDownMenu
import kotlinx.serialization.Serializable

@Serializable
object SetupAlbumDestination

@Composable
fun SetupAlbumRoute(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    permissionsResultLaunch: () -> Unit,
    permissionViewModel: PermissionViewModel,
) {
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(SetupGraph)
    }
    val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
    val albums by setupViewModel.albums.collectAsStateWithLifecycle(initialValue = emptyList())
    val granted by permissionViewModel.allPermissionsGranted.collectAsStateWithLifecycle()

    SetupAlbumScreen(onNextButtonClicked = {
        Log.d(TAG, "granted: $granted")
        if (granted) {
            navController.navigate(SetupPhotoDestination()) {
                popUpTo(SetupAlbumDestination) {
                    inclusive = true
                }
            }
        } else {
            permissionsResultLaunch()
        }
    }, checkForNameConflict = { name ->
        setupViewModel.albumName = name
        !albums.none { album ->
            album.directoryName == name
        }
    }, onLeaveAlertClicked = {
        navController.popBackStack()
    }, frequencyIsValid = { value ->
        setupViewModel.frequencyIsValid(value)
    })
}

@Composable
fun SetupAlbumScreen(
    onNextButtonClicked: () -> Unit,
    checkForNameConflict: (String) -> Boolean,
    frequencyIsValid: (String) -> Boolean,
    onLeaveAlertClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp, 64.dp)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var nameIsValid by remember { mutableStateOf(false) }
        var notificationsSet by remember { mutableStateOf(false) }
        var frequencyError by remember { mutableStateOf(false) }
        val nextButtonEnabled by remember {
            derivedStateOf {
                nameIsValid && notificationsSet
            }
        }

        val offset = 400f
        val titleBrush = Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primary
            ), tileMode = TileMode.Mirror, start = Offset(0f, 0f), end = Offset(offset, offset)
        )
        Text(
            textAlign = TextAlign.Center, style = TextStyle(
                brush = titleBrush,
                fontWeight = FontWeight.ExtraBold,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize
            ), text = stringResource(R.string.album_setup_title)
        )
        Spacer(modifier = Modifier.height(24.dp))
        NameTextField(checkForNameConflict = checkForNameConflict,
            onNameValidityChanged = { isValid -> nameIsValid = isValid })

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.notifications_options_label),
                textAlign = TextAlign.Center
            )
            DropDownMenu(
                items = listOf(
                    stringResource(R.string.i_don_t_need_a_reminder),
                    stringResource(R.string.everyday),
                    stringResource(R.string.every_2_days),
                    stringResource(R.string.once_a_week),
                    stringResource(R.string.every_3_weeks),
                    stringResource(R.string.once_a_month),
                    stringResource(R.string.every_6_months)
                ),
                stringResource(R.string.select_or_type_frequency),
                onValueChanged = { value ->
                    if (frequencyIsValid(value)) {
                        notificationsSet = true
                        frequencyError = false
                    } else {
                        frequencyError = true
                        notificationsSet = false
                    }
                },
                supportingText = stringResource(R.string.you_can_edit_the_available_options_to_e_g_every_3_days_or_every_2_weeks),
                isError = frequencyError
            )
        }

        if (nextButtonEnabled) {
            OutlinedButton(
                onClick = onNextButtonClicked
            ) {
                Text(text = stringResource(R.string.next))
            }
        }
    }

    BackHandlingDialog(
        title = stringResource(R.string.leave_album_creation),
        text = stringResource(R.string.if_you_exit_now_you_will_lose_your_creation_progress_are_you_sure_you_want_to_do_this),
        onLeaveClicked = onLeaveAlertClicked

    )
}

@Composable
fun NameTextField(
    checkForNameConflict: (String) -> Boolean, onNameValidityChanged: (Boolean) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    val errorText = stringResource(R.string.must_be_at_least_3_characters_long)
    val conflictText = stringResource(R.string.album_name_already_taken)
    val forbiddenText = stringResource(R.string.album_name_must_contain_only_letters_and_numbers)
    var isError by rememberSaveable { mutableStateOf(false) }
    var isConflict by rememberSaveable { mutableStateOf(false) }
    var isForbidden by remember { mutableStateOf(false) }
    OutlinedTextField(isError = isError || isConflict || isForbidden, supportingText = {
        if (isForbidden) Text(text = forbiddenText) else if (isError) Text(text = errorText) else if (isConflict) Text(
            text = conflictText
        )
    }, value = text, label = { Text(stringResource(R.string.album_name)) }, onValueChange = { newText ->
        text = newText
        isConflict = checkForNameConflict(newText)
        isError = text.length < 3
        isForbidden = text.fold(initial = false) { flag, char ->
            Log.d(
                TAG,
                "char: '$char', flag: $flag, isLetter: ${!char.isLetterOrDigit()}, isWhite: ${!char.isWhitespace()}, lettDigitWhite: ${((!char.isLetterOrDigit() || !char.isWhitespace()))}, comb:${(flag || (!char.isLetterOrDigit() || !char.isWhitespace()))}"
            )
            (flag || (!char.isLetterOrDigit() && !char.isWhitespace()))
        }
        Log.d(TAG, "isForbidden: $isForbidden")
        onNameValidityChanged(!isError && !isConflict && !isForbidden)
    })
}