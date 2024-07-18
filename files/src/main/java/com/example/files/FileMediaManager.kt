package com.example.files

import android.os.Environment
import java.io.File

class FileMediaManager() : MediaManagerInterface {

    override suspend fun getLatestPhotoFile(albumName: String): File? {
        val photoFiles = getPhotoFiles(albumName)
        return photoFiles.maxByOrNull { it.lastModified() }
    }

    override suspend fun getPhotoFiles(albumName: String): List<File> {
        return getAlbumFolderFile(albumName).listFiles()?.reversed() ?: mutableListOf()
    }

    override suspend fun removeLeftoverImagesFromNewAlbumFolder(
        albumName: String,
        firstPhotoPath: String
    ) {
        val firstFile = File(firstPhotoPath)
        getPhotoFiles(albumName).forEach { file -> if (file != firstFile) file.delete() }
    }

    override suspend fun deleteAlbum(albumName: String) {
        getPhotoFiles(albumName).forEach { file -> file.delete() }
        getAlbumFolderFile(albumName).delete()
    }

    override suspend fun deletePhoto(photoUri: String) {
        File(photoUri).delete()
    }

    override suspend fun getAlbumFolderFile(albumName: String): File {
        return File(
            Environment.getExternalStorageDirectory(),
            "$appPicturesPath/${albumName}"
        )
    }

    override suspend fun getMoviesFolderFile(albumName: String): File? {
        TODO("Not yet implemented")
    }


}