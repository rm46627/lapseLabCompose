package com.michredk.lapselab.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.michredk.database.Album
import com.michredk.lapselab.R
import com.michredk.lapselab.ui.common.DropDownMenu
import com.michredk.lapselab.ui.common.FreqUtils
import java.time.LocalTime

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
    val currentConfig = when (album.daysBetweenReminders) {
        0L -> stringResource(R.string.no_notifications_set)
        1L -> stringResource(R.string.daily_notifications_are_enabled_for, timeSet)
        else -> stringResource(
            R.string.you_will_be_notified_every_days_at,
            album.daysBetweenReminders,
            timeSet
        )
    }

    if (showDialog) {
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(background = AlertDialogDefaults.containerColor)) {
            AlertDialog(title = { Text(text = stringResource(R.string.configure_your_notifications)) }, text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        textAlign = TextAlign.Start, text = currentConfig, modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
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
                            if (FreqUtils.frequencyIsValid(value)) {
                                freq = value
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
                    if (freq.isNotEmpty() && freq != stringResource(R.string.i_don_t_need_a_reminder)) {
                        TimeInput(modifier = Modifier.padding(top = 4.dp), state = timePickerState)
                    }
                }
            }, onDismissRequest = dismissDialog, confirmButton = {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(R.string.apply),
                        color = if (!applyButtonEnabled) MaterialTheme.colorScheme.outlineVariant
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(enabled = applyButtonEnabled) {
                                onApplyClicked(
                                    LocalTime.of(
                                        timePickerState.hour,
                                        timePickerState.minute
                                    ), freq
                                )
                                dismissDialog()
                            }

                    )
                }
            }, dismissButton = {
                Text(text = stringResource(R.string.back), modifier = Modifier.clickable { dismissDialog() })
            })
        }
    }
}