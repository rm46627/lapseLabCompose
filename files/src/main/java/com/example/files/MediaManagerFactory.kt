package com.example.files

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

class MediaManagerFactory(context: Context) : MediaManagerInterface {

    private var manager: MediaManagerInterface =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            MediaStoreManager(context)
        } else {
            FileMediaManager()
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

    fun noMedia(albumName: String) {
        val folder = File("${Environment.getExternalStorageDirectory()}/$appPicturesPath/$albumName")
        folder.mkdirs()
        val noMediaFile = File(folder, ".nomedia").createNewFile()
    }
}