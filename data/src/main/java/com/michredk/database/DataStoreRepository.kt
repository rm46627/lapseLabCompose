package com.michredk.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

class DataStoreRepository(context: Context) {

    private object PreferencesKey {
        val onBoardingKey = booleanPreferencesKey(name = "on_boarding_completed")
        val contextMenuTipKey = booleanPreferencesKey(name = "context_menu_tip_completed")
        val filemanagerTipKey = booleanPreferencesKey(name = "filemanager_tip_completed")
        val ghostBtnTipKey = booleanPreferencesKey(name = "ghost_btn_tip_completed")
        val pagerViewMode = booleanPreferencesKey(name = "pager_view_mode")
        val lapseCreatorViewed = booleanPreferencesKey(name = "lapse_creator_viewed")
    }

    private val dataStore = context.dataStore

    suspend fun saveOnBoardingState(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.onBoardingKey] = completed
        }
    }

    suspend fun saveContextMenuTipState(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.contextMenuTipKey] = completed
        }
    }

    suspend fun saveFilemanagerTipState(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.filemanagerTipKey] = completed
        }
    }

    suspend fun saveGhostBtnTipState(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.ghostBtnTipKey] = completed
        }
    }

    suspend fun savePagerViewModeState(pagerMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.pagerViewMode] = pagerMode
        }
    }

    suspend fun saveLapseCreatorViewevState(viewed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.lapseCreatorViewed] = viewed
        }
    }

    suspend fun resetAllTips() {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.onBoardingKey] = false
            preferences[PreferencesKey.contextMenuTipKey] = false
            preferences[PreferencesKey.filemanagerTipKey] = false
            preferences[PreferencesKey.ghostBtnTipKey] = false
            preferences[PreferencesKey.lapseCreatorViewed] = false
        }
    }

    fun readOnBoardingState(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val onBoardingState = preferences[PreferencesKey.onBoardingKey] ?: false
                onBoardingState
            }
    }

    fun readContextMenuTipState(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val contextMenuTipState = preferences[PreferencesKey.contextMenuTipKey] ?: false
                contextMenuTipState
            }
    }

    fun readFilemanagerTipState(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val filemanagerTipState = preferences[PreferencesKey.filemanagerTipKey] ?: false
                filemanagerTipState
            }
    }

    fun readGhostBtnTipState(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val ghostBtnTipState = preferences[PreferencesKey.ghostBtnTipKey] ?: false
                ghostBtnTipState
            }
    }

    fun readGalleryViewMode(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val pagerViewModeState = preferences[PreferencesKey.pagerViewMode] ?: true
                pagerViewModeState
            }
    }

    fun readLapseCreatorViewed(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val lapseCretorViewedState = preferences[PreferencesKey.lapseCreatorViewed] ?: false
                lapseCretorViewedState
            }
    }


}