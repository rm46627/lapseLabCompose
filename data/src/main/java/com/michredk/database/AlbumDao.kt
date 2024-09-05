package com.michredk.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM Album")
    fun getAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM Album WHERE id = :albumId")
    fun getAlbum(albumId: Int): Flow<Album>

    @Query("SELECT * FROM Album WHERE directory_name = :albumName")
    fun getAlbum(albumName: String): Flow<Album>

    @Update
    suspend fun updateAlbum(album: Album)

    @Insert
    suspend fun addAlbum(album: Album)

    @Query("DELETE FROM Album WHERE directory_name = :albumName")
    suspend fun deleteAlbumByName(albumName: String)

    @Query("UPDATE Album SET directory_name = :newName WHERE directory_name = :oldName")
    suspend fun renameAlbumByName(oldName: String, newName: String)

    @Query("UPDATE Album SET cover_photo_path = :newPath WHERE directory_name = :albumName")
    suspend fun updateCoverPhoto(albumName: String, newPath: String)
    @Query("UPDATE Album SET video_width = :width AND video_height = :height WHERE directory_name = :albumName")
    suspend fun updateAlbumDimensions(albumName: String, width: Int, height: Int)
}
