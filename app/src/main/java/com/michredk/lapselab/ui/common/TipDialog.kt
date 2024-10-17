package com.michredk.lapselab.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.michredk.lapselab.R

// TODO: add gif to visualize usage

@Composable
fun TipDialog(
    title: String, content: @Composable () -> Unit, viewTipDialog: Boolean,
    saveTipViewed: () -> Unit, doOnConfirm: () -> Unit = {}, confirmButtonText: String = stringResource(
        R.string.ok)
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