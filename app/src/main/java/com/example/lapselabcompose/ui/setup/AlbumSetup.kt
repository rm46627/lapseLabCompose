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

@Composable
fun AlbumSetup(onNextButtonClicked: () -> Unit) {
    Scaffold { it ->
        Column(
            modifier = Modifier
                .padding(it)
                .padding(top = 32.dp, start = 16.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
//            verticalArrangement = Arrangement.
        ) {
            var text by remember { mutableStateOf("") }
            var nextButtonVisible by remember { mutableStateOf(false) }

            Text(text = stringResource(R.string.album_setup_title))
            OutlinedTextField(
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
                    nextButtonVisible = text.length > 3
                },
                modifier = Modifier.onFocusEvent {
                    if(it.isFocused) {
                        // TODO: Display hint about typing valid frequency
                    }
                }
            )
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
            if (nextButtonVisible) {
                OutlinedButton(onClick = onNextButtonClicked) {
                    Text(text = "Next")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSetup() {
    LapseLabComposeTheme {
        AlbumSetup() {}
    }
}