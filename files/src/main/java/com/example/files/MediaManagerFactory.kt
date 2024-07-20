package com.example.lapselab.files

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import com.example.files.MediaManagerInterface
import com.example.files.MediaStoreMediaManager
import java.io.File

class MediaManagerFactory(context: Context) : MediaManagerInterface {

    private var manager: MediaManagerInterface =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            MediaStoreMediaManager(context)
        } else {
            FileMediaManager(context)
        }

    override suspend fun saveBitmap(
        bitmap: Bitmap,
        subfolder: String,
        filename: String
    ): Pair<Uri?, String> {
        return manager.saveBitmap(bitmap, subfolder, filename)
    }

    override suspend fun getLatestPhotoFile(albumName: String): File? =
        manager.getLatestPhotoFile(albumName)

    override suspend fun getPhotoFiles(albumName: String): List<File> =
        manager.getPhotoFiles(albumName)

    // TODO: ask if user want to delete leftover photos
    override suspend fun removeLeftoverImagesFromNewAlbumFolder(albumName: String, firstPhotoPath: String) =
        manager.removeLeftoverImagesFromNewAlbumFolder(albumName, firstPhotoPath)

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