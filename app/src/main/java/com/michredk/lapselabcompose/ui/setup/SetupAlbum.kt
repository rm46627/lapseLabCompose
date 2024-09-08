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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.michredk.lapselabcompose.R
import com.michredk.lapselabcompose.TAG
import com.michredk.lapselabcompose.ui.SetupGraph
import com.michredk.lapselabcompose.ui.common.BackHandlingDialog
import com.michredk.lapselabcompose.ui.common.DropDownMenu
import kotlinx.serialization.Serializable

// TODO: Prevent creating album with forbidden names like 9:12

@Serializable
object SetupAlbumDestination

@Composable
fun SetupAlbumRoute(
    backStackEntry: NavBackStackEntry, navController: NavHostController
) {
    val parentEntry = remember(backStackEntry) {
        Log.d(TAG, "$$ albumSetup remember parent entry")
        navController.getBackStackEntry(SetupGraph)
    }
    val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
    val albums by setupViewModel.albums.collectAsStateWithLifecycle(initialValue = emptyList())

    SetupAlbumScreen(
        onNextButtonClicked = {
            navController.navigate(SetupPhotoDestination()) {
                popUpTo(SetupAlbumDestination) {
                    inclusive = true
                }
            }
        },
        checkForNameConflict = { name ->
            setupViewModel.albumName = name
            !albums.none { album ->
                album.directoryName == name
            }
        },
        onLeaveAlertClicked = {
            navController.popBackStack()
        },
        checkFrequencyValidity = { value ->
            setupViewModel.frequencyIsValid(value)
        }
    )
}

@Composable
fun SetupAlbumScreen(
    onNextButtonClicked: () -> Unit,
    checkForNameConflict: (String) -> Boolean,
    checkFrequencyValidity: (String) -> Boolean,
    onLeaveAlertClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp, 32.dp)
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

        Text(text = stringResource(R.string.album_setup_title))
        Spacer(modifier = Modifier.height(32.dp))
        NameTextField(
            checkForNameConflict = checkForNameConflict,
            onNameValidityChanged = { isValid -> nameIsValid = isValid }
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.notifications_options_label),
                textAlign = TextAlign.Center
            )
            DropDownMenu(
                items = listOf(
                    "I don't need a reminder",
                    "Everyday",
                    "Every 2 days",
                    "Once a week",
                    "Every 3 weeks",
                    "Once a month",
                    "Every 6 months"
                ), "Select or type frequency",
                onValueChanged = { value ->
                    if (value.isNotEmpty() && checkFrequencyValidity(value)) {
                        notificationsSet = true
                        frequencyError = false
                    } else {
                        frequencyError = true
                        notificationsSet = false
                    }
                },
                supportingText = "You can edit the available options to e.g. 'Every 3 days' or 'Every 2 weeks'",
                isError = frequencyError
            )
        }

        if (nextButtonEnabled) {
            OutlinedButton(
                onClick = onNextButtonClicked
            ) {
                Text(text = "Next")
            }
        }
    }

    BackHandlingDialog(
        title = "Leave album creation?",
        text = "If you exit now, you will lose your creation progress. Are you sure you want to do this?",
        onLeaveClicked = onLeaveAlertClicked

    )
}

@Composable
fun NameTextField(
    checkForNameConflict: (String) -> Boolean,
    onNameValidityChanged: (Boolean) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    val errorText = "Must be at least 3 characters long"
    val conflictText = "Album name already taken"
    var isError by rememberSaveable { mutableStateOf(false) }
    var isConflict by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(isError = isError || isConflict,
        supportingText = { if (isError) Text(text = errorText) else if (isConflict) Text(text = conflictText) },
        value = text,
        label = { Text("Album name") },
        onValueChange = { newText ->
            text = newText
            isConflict = checkForNameConflict(newText)
            isError = text.length < 3
            onNameValidityChanged(!isError && !isConflict)
        }
    )
}