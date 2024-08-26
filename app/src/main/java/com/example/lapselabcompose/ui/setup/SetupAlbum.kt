package com.example.lapselabcompose.ui.setup


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.lapselabcompose.AlarmItem
import com.example.lapselabcompose.AlarmScheduler
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.lapselabcompose.R
import com.example.lapselabcompose.TAG
import com.example.lapselabcompose.ui.SetupGraph
import com.example.lapselabcompose.ui.common.BackHandlingDialog
import com.example.lapselabcompose.ui.common.DropDownMenu
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

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

    SetupAlbumScreen(onNextButtonClicked = {
        navController.navigate(SetupPhotoDestination()) {
            popUpTo(SetupAlbumDestination) {
                inclusive = true
            }
        }
    }, checkForNameConflict = { name ->
        setupViewModel.albumName = name
        !albums.none { album ->
            album.directoryName == name
        }
    }, onLeaveAlertClicked = {
        navController.popBackStack()
    })
}

@Composable
fun SetupAlbumScreen(
    onNextButtonClicked: () -> Unit,
    checkForNameConflict: (String) -> Boolean,
    onLeaveAlertClicked: () -> Unit
) {
    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(top = 32.dp, start = 16.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            var nameIsValid by remember { mutableStateOf(false) }
            var notificationsSet by remember { mutableStateOf(false) }
            val nextButtonEnabled by remember {
                derivedStateOf {
                    nameIsValid && notificationsSet
                }
            }

            Text(text = stringResource(R.string.album_setup_title))

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
                        "Don't notify me",
                        "Every 1h",
                        "Every 12h",
                        "Everyday",
                        "Every 2 days",
                        "Every 3 days",
                        "Once a week",
                        "Once a month"
                    ), "Select or type frequency",
                    onValueChanged = { value ->
                       if(value.isNotEmpty()) {
                           notificationsSet = true
                       }
                    }
                )
            }

//            ////
//            // ALARM TEST
//            // TODO: add alarms to database
//            // TODO: restart alarms after reboot - https://www.youtube.com/watch?v=UiQWb3T2G-U
//            ////
//            val context = LocalContext.current
//            val alarmScheduler = AlarmScheduler(context)
//            var alarmItem: AlarmItem? = null
//            var secondsText by remember {
//                mutableStateOf("")
//            }
//            var message by remember {
//                mutableStateOf("")
//            }
//            Column {
//                OutlinedTextField(value = secondsText,
//                    onValueChange = { secondsText = it },
//                    label = {
//                        Text(
//                            text = "seconds"
//                        )
//                    })
//                OutlinedTextField(value = message, onValueChange = { message = it }, label = {
//                    Text(
//                        text = "message"
//                    )
//                })
//                Button(onClick = {
//                    alarmItem = AlarmItem(
//                        time = LocalDateTime.now().plusSeconds(secondsText.toLong()),
//                        message = message
//                    )
//                    alarmItem?.let(alarmScheduler::schedule)
//                    secondsText = ""
//                    message = ""
//                }) {
//                    Text(text = "Schedule")
//                }
//                Button(onClick = {
//                    alarmItem?.let(alarmScheduler::cancel)
//                }) {
//                    Text(text = "Cancel")
//                }
//            }

            if (nextButtonEnabled) {
                OutlinedButton(
                    onClick = onNextButtonClicked
                ) {
                    Text(text = "Next")
                }
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
        trailingIcon = {
            IconButton(onClick = {
                //TODO: Display modal explaining storing photos and how to exclude them from system app gallery
            }, content = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.album_name_info),
                )
            })
        },
        onValueChange = { newText ->
            text = newText
            isConflict = checkForNameConflict(newText)
            isError = text.length < 3
            onNameValidityChanged(!isError && !isConflict)
        },

        modifier = Modifier.onFocusEvent {
            if (it.isFocused) {
                // TODO: Display hint about typing valid frequency
            }
        })
}

@Preview
@Composable
fun PreviewSetup() {
    LapseLabComposeTheme {
        SetupAlbumScreen({}, { name -> false }, {})
    }
}