package com.example.lapselabcompose

import android.app.Application
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import dagger.hilt.android.HiltAndroidApp

// TODO: notification service for albums
// TODO: taken photos counter
// TODO: daily photos streak counter
// TODO: different frames for better streak and stars for photos counter

@HiltAndroidApp
class Application : Application(), CameraXConfig.Provider {
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR).build()
    }
}