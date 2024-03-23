package com.tsundoku.anilist.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tsundoku.extensions.getValue
import com.tsundoku.extensions.setValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): PreferencesRepository {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }

    val accessToken = dataStore.getValue(ACCESS_TOKEN_KEY, null)
    override suspend fun setAccessToken(accessToken: String?) = dataStore.setValue(ACCESS_TOKEN_KEY, accessToken)


    suspend fun logoutViewer() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
        }
    }
}