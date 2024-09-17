package com.michredk.lapselabcompose.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.michredk.database.Album
import com.michredk.lapselabcompose.ui.common.DropDownMenu
import com.michredk.lapselabcompose.ui.common.FreqUtils
import java.time.LocalTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureNotificationsDialog(
    showDialog: Boolean,
    album: Album,
    onApplyClicked: (LocalTime, String) -> Unit,
    dismissDialog: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialMinute = album.lastReminderSentOn.minute,
        initialHour = album.lastReminderSentOn.hour
    )
    var notificationsSet by remember { mutableStateOf(false) }
    var frequencyError by remember { mutableStateOf(false) }
    val applyButtonEnabled by remember {
        derivedStateOf {
            notificationsSet
        }
    }
    var freq by remember { mutableStateOf("") }

    val timeSet = "${LocalTime.of(timePickerState.hour, timePickerState.minute)}"
    val currentConfig = when(album.daysBetweenReminders) {
        0L -> "No notifications set."
        1L -> "Daily notifications are enabled for $timeSet."
        else -> "You will be notified every ${album.daysBetweenReminders} days at $timeSet."
    }

    if (showDialog) {
        AlertDialog(title = { Text(text = "Configure your notifications") }, text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(textAlign = TextAlign.Start, text = currentConfig, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
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
                if(freq.isNotEmpty() && freq != "I don't need a reminder"){
                    TimeInput(modifier = Modifier.padding(top = 4.dp), state = timePickerState)
                }
            }
        }, onDismissRequest = dismissDialog, confirmButton = {
            Row {
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Apply",
                    color = if (!applyButtonEnabled) MaterialTheme.colorScheme.outlineVariant
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(enabled = applyButtonEnabled) {
                            onApplyClicked(LocalTime.of(timePickerState.hour, timePickerState.minute), freq)
                            dismissDialog()
                        }

                )
            }
        }, dismissButton = {
            Text(text = "Back", modifier = Modifier.clickable { dismissDialog() })
        })
    }
}