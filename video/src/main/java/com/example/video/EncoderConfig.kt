package com.example.video

import android.media.MediaFormat
import java.io.File

data class EncoderConfig(
        var file: File,
        var mimeType: String = MediaFormat.MIMETYPE_VIDEO_AVC,
        var framesPerImage: Int = 1,
        var framesPerSecond: Float = 10F,
        var bitrate: Int = 1500000,
        var iFrameInterval: Int = 10
)

interface EncodingProgressListener {
        fun onFrameCreated(current: Int, end: Int)
}

interface EncodingResult

data class EncodingSuccess(
        val file: File
): EncodingResult

data class EncodingError(
        val message: String,
        val exception: Exception
): EncodingResult

data class EncodingFormatError(
        val message: String,
        val exception: Exception
): EncodingResult

