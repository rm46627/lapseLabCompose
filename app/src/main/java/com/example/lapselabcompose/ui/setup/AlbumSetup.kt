package com.example.lapselabcompose.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lapselabcompose.ui.theme.LapseLabComposeTheme
import com.example.lapselabcompose.R
import com.example.lapselabcompose.ui.common.DropDownMenu
import kotlinx.serialization.Serializable

@Serializable
object AlbumSetupDestination

@Composable
fun AlbumSetupRoute(
    onNextButtonClicked: () -> Unit
) {
    AlbumSetupScreen(onNextButtonClicked)
}

@Composable
fun AlbumSetupScreen(onNextButtonClicked: () -> Unit) {
    Scaffold { it ->
        Column(
            modifier = Modifier
                .padding(it)
                .padding(top = 32.dp, start = 16.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            var nextButtonEnabled by remember { mutableStateOf(true) }

            Text(text = stringResource(R.string.album_setup_title))
            // NameTextField
            NameTextField()

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.notifications_options_label),
                    textAlign = TextAlign.Center
                )
                DropDownMenu(
                    listOf(
                        "Every 1h",
                        "Every 12h",
                        "Everyday",
                        "Every 2 days",
                        "Every 3 days",
                        "Once a week",
                        "Once a month"
                    ), "Select or type frequency"
                ) {}
            }
            if (nextButtonEnabled) {
                OutlinedButton(onClick = onNextButtonClicked) {
                    Text(text = "Next")
                }
            }
        }
    }
}

@Composable
fun NameTextField() {
    var text by rememberSaveable { mutableStateOf("") }
    val errorText = "Must be at least 3 characters long"
    var isError by rememberSaveable { mutableStateOf(false)}
    OutlinedTextField(
        isError = isError,
        supportingText = { if (isError) Text(text = errorText) },
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
            isError = text.length < 3
        },

        modifier = Modifier.onFocusEvent {
            if(it.isFocused) {
                // TODO: Display hint about typing valid frequency
            }
        }
    )
}

@Preview
@Composable
fun PreviewSetup() {
    LapseLabComposeTheme {
        AlbumSetupScreen() {}
    }
}