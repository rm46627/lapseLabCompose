package com.michredk.lapselabcompose.ui.common

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.michredk.lapselabcompose.TAG

// TODO: add gif to visualize usage

@Composable
fun TipDialog(title: String, text: String, shouldViewTip: Boolean, saveTipViewed: () -> Unit) {

    var viewDialog by remember { mutableStateOf(shouldViewTip) }
    Log.d(TAG, "viewDialog: $viewDialog")
    if (viewDialog) {
        AlertDialog(
            title = { Text(text = title) },
            text = { Text(text = text) },
            onDismissRequest = {
                saveTipViewed()
                viewDialog = false
            },
            confirmButton = {
//                Row {
//                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Ok",
                        modifier = Modifier.clickable {
                            saveTipViewed()
                            viewDialog = false
                        })
//                }
            },
//            dismissButton = {
//                Text(
//                    text = "Stay",
//                    modifier = Modifier.clickable { viewDialog = false })
//            }
        )
    }
}