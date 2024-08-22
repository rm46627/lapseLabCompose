package com.example.video

import android.R.attr.height
import android.R.attr.width
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaCodecList.ALL_CODECS
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import java.io.File
import java.nio.ByteBuffer


const val TIMEOUT_USEC = 10000

class VideoEncoder(
    private val encoderConfig: EncoderConfig,
    width: Int,
    height: Int
) {

    private val mediaFormat: MediaFormat = run {
        val format = MediaFormat.createVideoFormat(encoderConfig.mimeType, width, height)
        Log.d(TAG, "MEDIA FORMAT width $width height $height mime ${encoderConfig.mimeType}")
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_BIT_RATE, encoderConfig.bitrate)
        format.setFloat(MediaFormat.KEY_FRAME_RATE, encoderConfig.framesPerSecond)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, encoderConfig.iFrameInterval)
        format
    }

    private val mediaCodec: MediaCodec = run {
        val codecs = MediaCodecList(ALL_CODECS)
        MediaCodec.createByCodecName(codecs.findEncoderForFormat(mediaFormat))
    }

    private val bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var mp4MuxerManager: Mp4MuxerManager = Mp4MuxerManager(
        encoderConfig.file.absolutePath,
        encoderConfig.framesPerSecond
    )

    private var surface: Surface? = null
    private var rect: Rect? = null

    fun start() {
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        surface = mediaCodec.createInputSurface()
        mediaCodec.start()
        drainCodec(false)
    }

    fun createFrame(image: File) {
        for (i in 0 until encoderConfig.framesPerImage) {
            val canvas = createCanvas()
            drawBitmapAndPostCanvas(BitmapFactory.decodeFile(image.path), canvas)
        }
    }

    private fun createCanvas(): Canvas? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            surface?.lockHardwareCanvas()
        } else {
            surface?.lockCanvas(rect)
        }
    }

    private fun drawBitmapAndPostCanvas(bitmapOrg: Bitmap, canvas: Canvas?) {
        // TODO : Dont rotate if images are horizontal
        // need to rotate image from portrait to horizontal
        val matrix = Matrix()
        matrix.postRotate(-90f)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg, bitmapOrg.width, bitmapOrg.height, true)
        val rotatedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
        scaledBitmap.recycle()
        canvas?.drawBitmap(rotatedBitmap, 0f, 0f, null)
        rotatedBitmap.recycle()
        postCanvasFrame(canvas)
    }


    private fun postCanvasFrame(canvas: Canvas?) {
        surface?.unlockCanvasAndPost(canvas)
        drainCodec(false)
    }

    private fun drainCodec(endOfStream: Boolean) {
        if (endOfStream) {
            mediaCodec.signalEndOfInputStream()
        }
        var encoderOutputBuffers: Array<ByteBuffer?>? = mediaCodec.getOutputBuffers()
        while (true) {
            val encoderStatus: Int =
                mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC.toLong())
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mediaCodec.getOutputBuffers()
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mp4MuxerManager.isStarted()) {
                    throw RuntimeException("format changed twice")
                }
                val newFormat: MediaFormat = mediaCodec.outputFormat
                mp4MuxerManager.start(newFormat)
            } else {
                val encodedData = encoderOutputBuffers?.get(encoderStatus)
                    ?: throw RuntimeException("encoderOutputBuffer  $encoderStatus was null")
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    bufferInfo.size = 0
                }
                if (bufferInfo.size != 0) {
                    if (!mp4MuxerManager.isStarted()) {
                        throw RuntimeException("muxer hasn't started")
                    }
                    mp4MuxerManager.muxVideoFrame(encodedData, bufferInfo)
                }
                mediaCodec.releaseOutputBuffer(encoderStatus, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
            }
        }
    }

    fun release() {
        drainCodec(true)
        mediaCodec.stop()
        mediaCodec.release()
        surface?.release()
        mp4MuxerManager.release()
    }

}