package com.michredk.lapselabcompose.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.michredk.database.Album
import com.michredk.lapselabcompose.ui.common.DropDownMenu
import com.michredk.lapselabcompose.ui.common.FreqUtils

@Composable
fun ConfigureNotificationsDialog(
    showDialog: Boolean,
    album: Album,
    onApplyClicked: (String) -> Unit,
    dismissDialog: () -> Unit
) {
    var notificationsSet by remember { mutableStateOf(false) }
    var frequencyError by remember { mutableStateOf(false) }
    val applyButtonEnabled by remember {
        derivedStateOf {
            notificationsSet
        }
    }
    var freq by remember { mutableStateOf("") }

    val currentConfig = when(album.daysBetweenReminders) {
        0L -> "No notifications set."
        1L -> "Daily notifications enabled."
        else -> "Notified every ${album.daysBetweenReminders} days"
    }

    if (showDialog) {
        AlertDialog(title = { Text(text = "Configure your notifications") }, text = {
            Column {
                Text(text = currentConfig)
                DropDownMenu(
                    items = listOf(
                        "I don't need a reminder",
                        "Everyday",
                        "Every 2 days",
                        "Once a week",
                        "Every 3 weeks",
                        "Once a month",
                        "Every 6 months"
                    ),
                    "Select or type frequency",
                    onValueChanged = { value ->
                        if (FreqUtils.frequencyIsValid(value)) {
                            freq = value
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
        }, onDismissRequest = dismissDialog, confirmButton = {
            Row {
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Apply",
                    color = if (!applyButtonEnabled) MaterialTheme.colorScheme.outlineVariant
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(enabled = applyButtonEnabled) {
                            onApplyClicked(freq)
                            dismissDialog()
                        }

                )
            }
        }, dismissButton = {
            Text(text = "Back", modifier = Modifier.clickable { dismissDialog() })
        })
    }
}