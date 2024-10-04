package com.michredk.files

import android.graphics.Bitmap
import android.net.Uri
import java.io.File

interface MediaManagerInterface {

    suspend fun saveBitmap(
        bitmap: Bitmap, subfolder: String
    ): Uri?

    suspend fun saveVideo(
        filename: String, subfolder: String
    ): Uri?

    suspend fun getLatestPhotoFile(albumName: String): File?
    suspend fun getLatestVideoFile(albumName: String): File?

    suspend fun getLatestVideoUri(albumName: String): Uri?

    suspend fun getPhotoFiles(albumName: String): List<File>

    suspend fun removeLeftoverPhotosFromNewAlbum(albumName: String, firstPhotoPath: String)

    suspend fun deleteAlbum(albumName: String)

    suspend fun deletePhoto(photoUri: String)
    suspend fun deleteLatestPhoto(albumName: String?)

    suspend fun getAlbumFolderFile(albumName: String): File?
    suspend fun getVideoFiles(albumName: String): MutableList<File>
}