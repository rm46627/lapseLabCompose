package com.example.video

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit


class Mp4MuxerManager(path: String, private val fps: Float){

    private val frameUsec: Long = run {
        (TimeUnit.SECONDS.toMicros(1L) / fps).toLong()
    }

    private val muxer: MediaMuxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

    private var started = false
    private var videoTrackIndex = 0
    private var videoFrames = 0
    private var finalVideoTime: Long = 0

    fun isStarted(): Boolean {
        return started
    }

    fun start(videoFormat: MediaFormat) {
        videoTrackIndex = muxer.addTrack(videoFormat)
        muxer.start()
        started = true
    }

    fun muxVideoFrame(encodedData: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        finalVideoTime = frameUsec * videoFrames++
        bufferInfo.presentationTimeUs = finalVideoTime
        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
    }

    fun release() {
        muxer.stop()
        muxer.release()
    }

    fun getVideoTime(): Long {
        return finalVideoTime
    }
}