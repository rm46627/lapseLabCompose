package com.example.files

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

const val TAG = "mytagforloging:Files"
const val appDir = "Pictures/LapseLab"
const val PHOTO_TYPE = "image/png"

fun getLapsesDir(albumName: String) = "Pictures/LapseLab/$albumName/lapses"

@RequiresApi(Build.VERSION_CODES.Q)
class MediaStoreMediaManager(private val context: Context) : MediaManagerInterface {

    private val mediaStoreCollection: Uri? =
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

    private fun createContentValues(name: String, subfolder: String): ContentValues =
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, PHOTO_TYPE)
            put(MediaStore.Images.Media.RELATIVE_PATH, subfolder)
        }

    override suspend fun saveBitmap(
        bitmap: Bitmap,
        subfolder: String,
        filename: String
    ): Pair<Uri?, String> =
        withContext(Dispatchers.IO) {
            val contentValues = createContentValues(filename, subfolder)
            var uri: Uri? = null
            try {
                uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                ) ?: throw IOException("Failed to create new MediaStore record")

                val stream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("Failed to open output stream")

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                if (uri != null) {
                    context.contentResolver.delete(uri, null, null)
                }
            }

            Pair(uri, filename)
        }

    private suspend fun getMediaStoreImageCursor(
        mediaStoreCollection: Uri, albumName: String? = null
    ): Cursor? {
        var cursor: Cursor?
        withContext(Dispatchers.IO) {
            val projection = arrayOf(imageDataColumnIndex, imageIdColumnIndex)
            val sortOrder = "DATE_ADDED DESC"
            val selection = "${MediaStore.Images.ImageColumns.RELATIVE_PATH} LIKE ?"
            val arg = if (albumName != null) {
                "%$appDir/$albumName/%"
            } else {
                "%$appDir/%"
            }
            val selectionArgs = arrayOf(arg)
            cursor = context.contentResolver.query(
                mediaStoreCollection, projection, selection, selectionArgs, sortOrder
            )
        }
        return cursor
    }

    override suspend fun getLatestPhotoFile(albumName: String): File? {
        var file: File?
        if (mediaStoreCollection == null) return null
        getMediaStoreImageCursor(mediaStoreCollection, albumName).use { cursor ->
            if (cursor?.moveToFirst() != true) return null
            val imageDataColumn = cursor.getColumnIndexOrThrow(imageDataColumnIndex)
            val contentFilePath = cursor.getString(imageDataColumn)
            file = File(contentFilePath)
        }
        return file
    }

    override suspend fun getPhotoFiles(albumName: String): MutableList<File> {
        val files = mutableListOf<File>()
        if (mediaStoreCollection == null) return files
        if (albumName != null) {
            getMediaStoreImageCursor(mediaStoreCollection, albumName)
        } else {
            getMediaStoreImageCursor(mediaStoreCollection)
        }.use { cursor ->
            val imageDataColumn = cursor?.getColumnIndexOrThrow(imageDataColumnIndex)
            val imageIdColumn = cursor?.getColumnIndexOrThrow(imageIdColumnIndex)
            if (cursor != null && imageDataColumn != null && imageIdColumn != null) {
                while (cursor.moveToNext()) {
                    val contentFilePath = cursor.getString(imageDataColumn)
                    files.add(File(contentFilePath))
                }
            }
        }
        return files
    }

    override suspend fun removeLeftoverPhotosFromNewAlbum(
        albumName: String, firstPhotoPath: String
    ) {
        getPhotoFiles(albumName).forEach { file ->
            val filePath = file.absolutePath
            if (filePath != firstPhotoPath) {
                file.delete()
            }
        }
    }

    override suspend fun deleteAlbum(albumName: String) {
        val files = getPhotoFiles(albumName)
        if (files.size == 0) {
            return
        }
        val albumFolder = files[0].parentFile
        files.forEach { file ->
            file.delete()
        }
        albumFolder!!.delete()
    }

    override suspend fun deletePhoto(photoUri: String) {
        Log.d(TAG, "uri: $photoUri")
        File(photoUri).delete()
        MediaScannerConnection.scanFile(
            context, arrayOf(photoUri), null, null
        )
    }

    override suspend fun getAlbumFolderFile(albumName: String): File? =
        getLatestPhotoFile(albumName)?.parentFile

    override suspend fun getMoviesFolderFile(albumName: String): File? {
        TODO("Not yet implemented")
    }


    companion object {
        private const val imageDataColumnIndex = MediaStore.Images.Media.DATA
        private const val imageIdColumnIndex = MediaStore.Images.Media._ID
    }
}