package com.example.video

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaFormat
import android.os.Environment
import android.util.Log
import com.example.database.Album
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

const val TAG = "mytagforloging:Video"
const val FILES_NAME_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
const val APP_MOVIE_PATH = "Movies/LapseLab"

class LapseCreator(private val context: Context, private val album: Album) {

    fun createVideo(photos: List<File>): String {
        val date =
            SimpleDateFormat(FILES_NAME_DATE_FORMAT, Locale.US).format(System.currentTimeMillis())
        val name = "${album.directoryName}-${photos.size}images-$date"
        val folder =
            File("${Environment.getExternalStorageDirectory()}/$APP_MOVIE_PATH/${album.directoryName}")
        folder.mkdirs()
        val videoFile = File(folder, "$name.mp4")

        var (height, width) = getImageDimensions(photos[0])

        val encoderConfig = EncoderConfig(
            videoFile,
            MediaFormat.MIMETYPE_VIDEO_AVC,
            1,
            1F,
            1500000
        )

        val mediaProcessor = MediaProcessor(context, encoderConfig)
        mediaProcessor.setOnEncodingProgressListener(object : EncodingProgressListener {
            override fun onFrameCreated(current: Int, end: Int) {
                Log.d(TAG, "Muxing progress: $current / $end")
            }
        })

        var i = 0
        while (true) {
            Log.d(TAG, "encode: $width x $height")
            when (val result = mediaProcessor.encodeMp4(photos, width, height, listOf())) {
                is EncodingError -> {
                    Log.d(TAG, result.message)
                }

                is EncodingFormatError -> {
                    width -= 1
                    height -= 5
                    if(i++ > 200) {
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


    private fun getImageDimensions(imageFile: File): Pair<Int, Int> {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        return Pair(bitmap.height, bitmap.width)
    }
}