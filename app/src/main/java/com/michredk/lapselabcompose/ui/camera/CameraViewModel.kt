package com.michredk.lapselabcompose.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michredk.database.Album
import com.michredk.database.Repository
import com.michredk.lapselabcompose.services.alarm.AlarmScheduler
import com.michredk.lapselabcompose.ui.common.FreqUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(): ViewModel(){
    var albumName: String? = null
    var bitmap: Bitmap? = null
}