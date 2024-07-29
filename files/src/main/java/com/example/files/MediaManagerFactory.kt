package com.example.lapselab.files

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import com.example.files.MediaManagerInterface
import com.example.files.MediaStoreMediaManager
import java.io.File

const val FILES_NAME_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

class MediaManagerFactory(context: Context) : MediaManagerInterface {

    private var manager: MediaManagerInterface =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            MediaStoreMediaManager(context)
        } else {
            FileMediaManager(context)
        }

    override suspend fun saveBitmap(
        bitmap: Bitmap,
        subfolder: String
    ): Uri? {
        return manager.saveBitmap(bitmap, subfolder)
    }

    override suspend fun saveVideo(filename: String, subfolder: String): Uri? {
        return manager.saveVideo(filename, subfolder)
    }

    override suspend fun getLatestPhotoFile(albumName: String): File? =
        manager.getLatestPhotoFile(albumName)

    override suspend fun getLatestVideoFile(albumName: String): File? =
        manager.getLatestVideoFile(albumName)

    override suspend fun getLatestVideoUri(albumName: String): Uri? =
        manager.getLatestVideoUri(albumName)

    override suspend fun getPhotoFiles(albumName: String): List<File> =
        manager.getPhotoFiles(albumName)

    // TODO: ask if user want to delete leftover photos
    override suspend fun removeLeftoverPhotosFromNewAlbum(albumName: String, firstPhotoPath: String) =
        manager.removeLeftoverPhotosFromNewAlbum(albumName, firstPhotoPath)

    override suspend fun deleteAlbum(albumName: String) =
        manager.deleteAlbum(albumName)

    override suspend fun deletePhoto(photoUri: String) {
        manager.deletePhoto(photoUri)
    }

    override suspend fun getAlbumFolderFile(albumName: String): File? =
        manager.getAlbumFolderFile(albumName)

    override suspend fun getMoviesFolderFile(albumName: String): File? {
        TODO("Not yet implemented")
    }

    suspend fun getVideoFile(albumName: String, fileName: String): File {
        return File(getAlbumFolderFile(albumName), fileName)
    }
}