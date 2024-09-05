package com.michredk.lapselabcompose.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BackHandlingDialog(title: String, text: String, onLeaveClicked: () -> Unit) {
    var viewExitDialog by remember { mutableStateOf(false) }
    BackHandler {
        viewExitDialog = true
    }
    if (viewExitDialog) {
        AlertDialog(
            title = { Text(text = title) },
            text = { Text(text = text) },
            onDismissRequest = { viewExitDialog = false },
            confirmButton = {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Leave",
                        modifier = Modifier.clickable {
                            onLeaveClicked()
                            viewExitDialog = false
                        })
                }
            },
            dismissButton = {
                Text(
                    text = "Stay",
                    modifier = Modifier.clickable { viewExitDialog = false })
            }
        )
    }
}