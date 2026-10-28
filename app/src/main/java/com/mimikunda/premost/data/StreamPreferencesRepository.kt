package com.mimikunda.premost.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stream_settings")

class StreamPreferencesRepository(private val context: Context) {

    companion object {
        private val STREAM_URL = stringPreferencesKey("stream_url")
        private val HTTP_REFERER = stringPreferencesKey("http_referer")
        private val HTTP_ORIGIN = stringPreferencesKey("http_origin")
    }

    val streamConfigFlow: Flow<StreamConfig?> = context.dataStore.data.map { preferences ->
        val url = preferences[STREAM_URL]
        if (url.isNullOrBlank()) {
            null
        } else {
            StreamConfig(
                url = url,
                referer = preferences[HTTP_REFERER] ?: "",
                origin = preferences[HTTP_ORIGIN] ?: ""
            )
        }
    }

    suspend fun saveStreamConfig(url: String, referer: String, origin: String) {
        context.dataStore.edit { preferences ->
            preferences[STREAM_URL] = url.trim()
            preferences[HTTP_REFERER] = referer.trim()
            preferences[HTTP_ORIGIN] = origin.trim()
        }
    }

    suspend fun clearStreamConfig() {
        context.dataStore.edit { preferences ->
            preferences.remove(STREAM_URL)
            preferences.remove(HTTP_REFERER)
            preferences.remove(HTTP_ORIGIN)
        }
    }
}
