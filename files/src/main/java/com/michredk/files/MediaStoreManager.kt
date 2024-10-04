package com.michredk.files

import android.content.ContentUris
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
import com.michredk.lapselab.files.FILES_NAME_DATE_FORMAT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

const val TAG = "mytagforloging:MediaStore"
const val appPicturesDir = "Pictures/LapseLab"
const val appMoviesDir = "Movies/LapseLab"
const val PHOTO_TYPE = "image/png"
const val VIDEO_TYPE = "video/mp4"

class MediaStoreMediaManager(private val context: Context) : MediaManagerInterface {

    ////
    // COMMON
    ////

    companion object {
        private const val imageDataColumnIndex = MediaStore.Images.Media.DATA
        private const val imageIdColumnIndex = MediaStore.Images.Media._ID
        private const val videoDataColumnIndex = MediaStore.Video.Media.DATA
        private const val videoIdColumnIndex = MediaStore.Video.Media._ID
    }

    private val mediaStoreImageCollection: Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    private val mediaStoreVideoCollection: Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

    private fun createContentValues(
        name: String,
        subfolder: String,
        filetype: String
    ): ContentValues =
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, filetype)
            when (filetype) {
                PHOTO_TYPE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, subfolder)
                } else {
                    val albumFolder = File(
                        Environment.getExternalStorageDirectory(), subfolder
                    )
                    if (!albumFolder.exists()) {
                        albumFolder.mkdirs()
                    }
                    put(
                        MediaStore.Images.Media.DATA,
                        "${Environment.getExternalStorageDirectory()}/$subfolder/$name.png"
                    )
                }

                VIDEO_TYPE ->
//                    put(MediaStore.Video.Media.RELATIVE_PATH, subfolder)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, subfolder)
                } else {
                    val albumFolder = File(
                        Environment.getExternalStorageDirectory(), subfolder
                    )
                    if (!albumFolder.exists()) {
                        albumFolder.mkdirs()
                    }
                    put(
                        MediaStore.Video.Media.DATA,
                        "${Environment.getExternalStorageDirectory()}/$subfolder/$name"
                    )
                }
            }
        }

    override suspend fun deleteAlbum(albumName: String) {
        //Photo
        val photoFiles = getPhotoFiles(albumName)
        if (photoFiles.size == 0) {
            return
        }
        val photoAlbumFolder = photoFiles[0].parentFile
        photoFiles.forEach { file ->
            file.delete()
        }
        photoAlbumFolder!!.delete()
        //Video
        val videoFiles = getVideoFiles(albumName)
        if (videoFiles.size == 0) {
            return
        }
        val videoAlbumFolder = videoFiles[0].parentFile
        videoFiles.forEach { file ->
            file.delete()
        }
        videoAlbumFolder!!.delete()
    }

    ////
    // IMAGES
    ////

    override suspend fun saveBitmap(
        bitmap: Bitmap,
        subfolder: String
    ): Uri? =
        withContext(Dispatchers.IO) {
            val filename =
                SimpleDateFormat(
                    FILES_NAME_DATE_FORMAT,
                    Locale.US
                ).format(System.currentTimeMillis())
            val contentValues = createContentValues(filename, subfolder, PHOTO_TYPE)
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
            uri
        }

    private suspend fun getMediaStoreImageCursor(
        mediaStoreCollection: Uri, albumName: String? = null
    ): Cursor? {
        var cursor: Cursor?
        withContext(Dispatchers.IO) {
            val projection = arrayOf(imageDataColumnIndex, imageIdColumnIndex)
            val sortOrder = "DATE_ADDED DESC"
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Images.ImageColumns.RELATIVE_PATH} LIKE ?"
            } else {
                "${MediaStore.Images.ImageColumns.DATA} LIKE ?"
            }
            val arg = if (albumName != null) {
                "%$appPicturesDir/$albumName/%"
            } else {
                "%$appPicturesDir/%"
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
        if (mediaStoreImageCollection == null) return null
        getMediaStoreImageCursor(mediaStoreImageCollection, albumName).use { cursor ->
            if (cursor?.moveToFirst() != true) return null
            val imageDataColumn = cursor.getColumnIndexOrThrow(imageDataColumnIndex)
            val contentFilePath = cursor.getString(imageDataColumn)
            file = File(contentFilePath)
        }
        return file
    }

    override suspend fun deleteLatestPhoto(albumName: String?) {
        val albumNameSafe = albumName ?: return
        val file = getLatestPhotoFile(albumNameSafe) ?: return
        deletePhoto(file.absolutePath)
    }

    override suspend fun getPhotoFiles(albumName: String): MutableList<File> {
        val files = mutableListOf<File>()
        if (mediaStoreImageCollection == null) return files
        if (albumName != null) {
            getMediaStoreImageCursor(mediaStoreImageCollection, albumName)
        } else {
            getMediaStoreImageCursor(mediaStoreImageCollection)
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

    override suspend fun deletePhoto(photoUri: String) {
        Log.d(TAG, "uri: $photoUri")
        File(photoUri).delete()
        MediaScannerConnection.scanFile(
            context, arrayOf(photoUri), null, null
        )
    }

    override suspend fun getAlbumFolderFile(albumName: String): File? =
        getLatestPhotoFile(albumName)?.parentFile

    ////
    // VIDEO
    ////

    override suspend fun saveVideo(
        filename: String,
        subfolder: String
    ): Uri =
        withContext(Dispatchers.IO) {
            val contentValues = createContentValues(filename, subfolder, VIDEO_TYPE)
            var uri: Uri? = null
            try {
                uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues
                ) ?: throw IOException("Failed to create new MediaStore record")
            } catch (e: IOException) {
                throw e
            }
            Log.d(TAG, "created content values: $uri,,, filename: $filename")
            uri
        }

    override suspend fun getLatestVideoFile(albumName: String): File? {
        var file: File? = null
        var paths: Array<String> = arrayOf()
        if (mediaStoreVideoCollection == null) return null
        getMediaStoreVideoCursor(mediaStoreVideoCollection, albumName).use { cursor ->
            if (cursor?.moveToFirst() != true) return null
            do {
                val videoDataColumn = cursor.getColumnIndexOrThrow(videoDataColumnIndex)
                val contentFilePath = cursor.getString(videoDataColumn)
                Log.d(TAG, "FILE FOUND $contentFilePath")
                if (contentFilePath.takeLast(7).contains("(1)")) {
                    paths += contentFilePath
                } else {
                    file = File(contentFilePath)
                    break
                }
            } while (cursor.moveToNext())
        }
        MediaScannerConnection.scanFile(
            context, paths, null, null
        )

        return file
    }

    override suspend fun getLatestVideoUri(albumName: String): Uri? {
        var fileUri: Uri?
        if (mediaStoreVideoCollection == null) return null
        getMediaStoreVideoCursor(mediaStoreVideoCollection, albumName).use { cursor ->
            if (cursor?.moveToFirst() != true) return null
            val videoDataColumn = cursor.getColumnIndexOrThrow(videoIdColumnIndex)
            fileUri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                cursor.getLong(videoDataColumn)
            )
        }
        return fileUri
    }

    private suspend fun getMediaStoreVideoCursor(
        mediaStoreCollection: Uri, albumName: String? = null
    ): Cursor? {
        var cursor: Cursor?
        withContext(Dispatchers.IO) {
            val projection = arrayOf(videoDataColumnIndex, videoIdColumnIndex)
            val sortOrder = "DATE_ADDED DESC"
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Video.VideoColumns.RELATIVE_PATH} LIKE ?"
            } else {
                "${MediaStore.Video.VideoColumns.DATA} LIKE ?"
            }
            val arg = if (albumName != null) {
                "%$appMoviesDir/$albumName/%"
            } else {
                "%$appMoviesDir/%"
            }
            val selectionArgs = arrayOf(arg)
            cursor = context.contentResolver.query(
                mediaStoreCollection, projection, selection, selectionArgs, sortOrder
            )
        }
        return cursor
    }

    override suspend fun getVideoFiles(albumName: String): MutableList<File> {
        val files = mutableListOf<File>()
        if (mediaStoreVideoCollection == null) return files
        if (albumName != null) {
            getMediaStoreVideoCursor(mediaStoreVideoCollection, albumName)
        } else {
            getMediaStoreVideoCursor(mediaStoreVideoCollection)
        }.use { cursor ->
            val videoDataColumn = cursor?.getColumnIndexOrThrow(videoDataColumnIndex)
            val videoIdColumn = cursor?.getColumnIndexOrThrow(videoIdColumnIndex)
            if (cursor != null && videoDataColumn != null && videoIdColumn != null) {
                while (cursor.moveToNext()) {
                    val contentFilePath = cursor.getString(videoDataColumn)
                    files.add(File(contentFilePath))
                }
            }
        }
        return files
    }
}