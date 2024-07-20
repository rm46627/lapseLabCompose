package com.example.lapselabcompose.ui.common

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.files.PHOTO_TYPE
import com.example.lapselabcompose.R

fun ShareImage(uri : Uri, filename: String, context: Context) {
    val chooser = Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            clipData =
                ClipData.newRawUri(filename, uri)  // to fix Security Exception
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = PHOTO_TYPE
        }, context.getString(R.string.share_where)
    )
    context.startActivity(chooser)
}