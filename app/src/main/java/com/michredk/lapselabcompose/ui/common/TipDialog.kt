package com.michredk.lapselabcompose.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
    title: String, content: @Composable () -> Unit, viewTipDialog: Boolean,
    saveTipViewed: () -> Unit, doOnConfirm: () -> Unit = {}, confirmButtonText: String = "Ok"
) {
    if (viewTipDialog) {
        AlertDialog(
            title = { Text(text = title) },
            text = content,
            onDismissRequest = {
                saveTipViewed()
            },
            confirmButton = {
                Text(text = confirmButtonText, modifier = Modifier.clickable {
                    saveTipViewed()
                    doOnConfirm()
                })
            },
        )
    }
}