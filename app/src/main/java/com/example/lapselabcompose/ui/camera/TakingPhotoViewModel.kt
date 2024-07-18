package com.example.lapselabcompose.ui.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TakingPhotoViewModel @Inject constructor(): ViewModel(){
    var albumName: String? = null
    var bitmap: Bitmap? = null

}