package com.michredk.lapselab.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michredk.database.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(private val dataStore: DataStoreRepository): ViewModel(){
    var albumName: String? = null
    var bitmap: Bitmap? = null

    val isGhostBtnTipCompleted = dataStore.readGhostBtnTipState()

    fun updateGhostBtnTipValue(isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.saveGhostBtnTipState(isCompleted)
        }
    }
}