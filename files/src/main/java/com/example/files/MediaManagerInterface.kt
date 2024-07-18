package com.example.files

import java.io.File

interface MediaManagerInterface {

    suspend fun getLatestPhotoFile(albumName: String): File?

    suspend fun getPhotoFiles(albumName: String): List<File>

    suspend fun removeLeftoverImagesFromNewAlbumFolder(albumName: String, firstPhotoPath: String)

    suspend fun deleteAlbum(albumName: String)

    suspend fun deletePhoto(photoUri: String)

    suspend fun getAlbumFolderFile(albumName: String) : File?
    suspend fun getMoviesFolderFile(albumName: String) : File?
}