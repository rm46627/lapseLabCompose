package com.example.database

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class Repository @Inject constructor(
    private val albumDao: AlbumDao
) {
    fun getAlbums(): Flow<List<Album>> {
        return albumDao.getAlbums()
    }

    fun getAlbum(id: Int): Flow<Album> = albumDao.getAlbum(id)

    fun getAlbum(name: String): Flow<Album> = albumDao.getAlbum(name)

    suspend fun updateAlbum(album: Album) = albumDao.updateAlbum(album)

    suspend fun addAlbum(album: Album) {
        return albumDao.addAlbum(album)
    }

    suspend fun deleteAlbum(albumName: String) {
        return albumDao.deleteAlbumByName(albumName)
    }

    suspend fun renameAlbum(oldName: String, newName: String) {
        return albumDao.renameAlbumByName(oldName, newName)
    }

    suspend fun updateCoverPhoto(albumName: String, newPath: String) {
        return albumDao.updateCoverPhoto(albumName, newPath)
    }

}