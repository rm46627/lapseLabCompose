package com.michredk.lapselabcompose.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// TODO: add gif to visualize usage

@Composable
fun TipDialog(
    title: String, text: String, viewTipDialog: Boolean,
    saveTipViewed: () -> Unit
) {
    if (viewTipDialog) {
        AlertDialog(
            title = { Text(text = title) },
            text = { Text(text = text) },
            onDismissRequest = {
                saveTipViewed()
            },
            confirmButton = {
                Text(text = "Ok", modifier = Modifier.clickable {
                    saveTipViewed()
                })
            },
        )
    }
}