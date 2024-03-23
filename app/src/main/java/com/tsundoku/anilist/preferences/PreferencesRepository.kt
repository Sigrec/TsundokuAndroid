package com.tsundoku.anilist.preferences

import androidx.datastore.preferences.core.Preferences

interface PreferencesRepository {
    suspend fun setAccessToken(accessToken: String?): Preferences
}