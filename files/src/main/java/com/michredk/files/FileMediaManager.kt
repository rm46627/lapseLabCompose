//package com.michredk.lapselab.files
//
//import android.content.ContentValues
//import android.content.Context
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Environment
//import android.provider.MediaStore
//import com.michredk.files.MediaManagerInterface
//import com.michredk.files.appPicturesDir
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class FileMediaManager(private val context: Context) : MediaManagerInterface {
//
//    private fun createContentValues(name: String, subfolder: String): ContentValues =
//        ContentValues().apply {
//            val albumFolder = File(
//                Environment.getExternalStorageDirectory(), subfolder
//            )
//            if (!albumFolder.exists()) {
//                albumFolder.mkdirs()
//            }
//            put(
//                MediaStore.Images.Media.DATA,
//                "${Environment.getExternalStorageDirectory()}/$subfolder/$name.png"
//            )
//        }
//
//
//    override suspend fun saveBitmap(
//        bitmap: Bitmap,
//        subfolder: String
//    ): Uri? =
//        withContext(Dispatchers.IO) {
//            val filename =
//                SimpleDateFormat(FILES_NAME_DATE_FORMAT, Locale.US).format(System.currentTimeMillis())
//            val contentValues = createContentValues(filename, subfolder)
//            var uri: Uri? = null
//            try {
//                uri = context.contentResolver.insert(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
//                ) ?: throw IOException("Failed to create new MediaStore record")
//
//                val stream = context.contentResolver.openOutputStream(uri)
//                    ?: throw IOException("Failed to open output stream")
//
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//                stream.flush()
//                stream.close()
//            } catch (e: IOException) {
//                if (uri != null) {
//                    context.contentResolver.delete(uri, null, null)
//                }
//            }
//
//            uri
//        }
//
//    override suspend fun saveVideo(filename: String, subfolder: String): Uri? {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getLatestPhotoFile(albumName: String): File? {
//        val photoFiles = getPhotoFiles(albumName)
//        return photoFiles.maxByOrNull { it.lastModified() }
//    }
//
//    override suspend fun getLatestVideoFile(albumName: String): File? {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getLatestVideoUri(albumName: String): Uri? {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getPhotoFiles(albumName: String): List<File> {
//        return getAlbumFolderFile(albumName).listFiles()?.reversed() ?: mutableListOf()
//    }
//
//    override suspend fun removeLeftoverPhotosFromNewAlbum(
//        albumName: String,
//        firstPhotoPath: String
//    ) {
//        val firstFile = File(firstPhotoPath)
//        getPhotoFiles(albumName).forEach { file -> if (file != firstFile) file.delete() }
//    }
//
//    override suspend fun deleteAlbum(albumName: String) {
//        getPhotoFiles(albumName).forEach { file -> file.delete() }
//        getAlbumFolderFile(albumName).delete()
//    }
//
//    override suspend fun deletePhoto(photoUri: String) {
//        File(photoUri).delete()
//    }
//
//    override suspend fun deleteLatestPhoto(albumName: String?) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getAlbumFolderFile(albumName: String): File {
//        return File(
//            Environment.getExternalStorageDirectory(),
//            "$appPicturesDir/${albumName}"
//        )
//    }
//
//    override suspend fun getVideoFiles(albumName: String): MutableList<File> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun getMoviesFolderFile(albumName: String): File? {
//        TODO("Not yet implemented")
//    }
//}