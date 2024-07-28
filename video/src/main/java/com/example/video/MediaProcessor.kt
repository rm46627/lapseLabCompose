package com.example.video

import android.content.Context
import android.media.MediaCodecList
import android.media.MediaCodecList.REGULAR_CODECS
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File
import java.io.IOException

class FormatError

class MediaProcessor(private val context: Context, private val file: File) {
    constructor(context: Context, config: EncoderConfig) : this(context, config.file) {
        encoderConfig = config
    }

    // Initialize a default configuration
    private var encoderConfig: EncoderConfig = EncoderConfig(file)
    private var encodingProgressListener: EncodingProgressListener? = null

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    fun encodeMp4(imageList: List<File>, width: Int, height: Int, effects: List<Effect> = listOf() ): EncodingResult {
        val videoEncoder: VideoEncoder?
        try {
            videoEncoder = VideoEncoder(encoderConfig, width, height)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return EncodingFormatError("Probably findEncoderForFormat() error", e)
        }

        try {
            videoEncoder.start()
        } catch (e: IOException) {
            e.printStackTrace()
            return EncodingError("Start encoder failed", e)
        }

        for (i in imageList.indices) {
            videoEncoder.createFrame(imageList[i])
            encodingProgressListener?.onFrameCreated(i+1, imageList.size)
        }

        videoEncoder.release()

        transformVideo(effects)


        return EncodingSuccess(file)
    }

    @OptIn(UnstableApi::class)
    private fun transformVideo(effects: List<Effect>) {
        val transformerListener: Transformer.Listener =
            object : Transformer.Listener {
                override fun onCompleted(composition: Composition, result: ExportResult) {

                }

                override fun onError(
                    composition: Composition, result: ExportResult,
                    exception: ExportException
                ) {

                }
            }
        val mediaItem = MediaItem.fromUri(file.absolutePath)
        val transformer = Transformer.Builder(context)
            .addListener(transformerListener)
            .build()
        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setEffects(
                Effects(
                    /* audioProcessors= */ listOf(),
                    /* videoEffects= */
//                    listOf(
//                        ScaleAndRotateTransformation.Builder()
//                            .setRotationDegrees(-90f)
//                            .build()
//                    )
                    effects
                )
            ).build()
        val composition =
            Composition.Builder(EditedMediaItemSequence(editedMediaItem)).build()
        transformer.start(composition, file.absolutePath)
    }

    fun setOnEncodingProgressListener(encodingProgressListener: EncodingProgressListener) {
        this.encodingProgressListener = encodingProgressListener
    }
}

fun isCodecSupported(mimeType: String?): Boolean {
    val codecs = MediaCodecList(REGULAR_CODECS)
    for (codec in codecs.codecInfos) {
        if (!codec.isEncoder) {
            continue
        }
        for (type in codec.supportedTypes) {
            if (type == mimeType) return true
        }
    }
    return false
}