package com.michredk.video

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaFormat
import android.os.Environment
import android.util.Log
import com.michredk.database.Album
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

const val TAG = "mytagforloging"
const val FILES_NAME_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
const val APP_MOVIE_PATH = "Movies/LapseLab"

class LapseCreator(private val context: Context, private val album: Album) {

    suspend fun createVideo(photos: List<File>, framesPerImage: Int, bitrate: Int): String {
        val date =
            SimpleDateFormat(FILES_NAME_DATE_FORMAT, Locale.US).format(System.currentTimeMillis())
        val name = "${album.directoryName}-${photos.size}images-$date"
        val folder =
            File("${Environment.getExternalStorageDirectory()}/$APP_MOVIE_PATH/${album.directoryName}")
        folder.mkdirs()
        val videoFile = File(folder, "$name.mp4")
        Log.d(TAG, "creating videofile, ${videoFile.name}")

        // TODO : Dont rotate if images are horizontal
        // NEED TO ROTATE IMAGES FOR ENCODING height = width, width = height
        // from portrait to horizontal
        var (width, height) = getImageDimensions(photos[0])

        val encoderConfig = EncoderConfig(
            videoFile,
            MediaFormat.MIMETYPE_VIDEO_AVC,
            framesPerImage,
            10F,
            bitrate
        )

        val mediaProcessor = MediaProcessor(context, encoderConfig)
        mediaProcessor.setOnEncodingProgressListener(object : EncodingProgressListener {
            override fun onFrameCreated(current: Int, end: Int) {
                Log.d(TAG, "Muxing progress: $current / $end")
            }
        })

        var i = 0
        while (true) {
            when (val result = mediaProcessor.encodeMp4(photos, width, height, listOf())) {
                is EncodingError -> {
                    Log.d(TAG, result.message)
                }

                is EncodingFormatError -> {
                    width -= 1
                    height -= 5
                    if(i++ > 5) {
                        break
                    }
                }

                is EncodingSuccess -> {
                    Log.d(TAG, "Success!!!")
                    // TODO: update dimensions
                    break
                }
            }
        }

        return encoderConfig.file.name

    }
}

fun getImageDimensions(imageFile: File): Pair<Int, Int> {
    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
    return Pair(bitmap.height, bitmap.width)
}