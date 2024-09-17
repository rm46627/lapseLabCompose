package com.michredk.lapselabcompose.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(): ViewModel(){
    var albumName: String? = null
    var bitmap: Bitmap? = null

}