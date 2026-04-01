package com.androidbolts.minitiktok.core.storage.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.androidbolts.minitiktok.core.storage.prefs.PrefKeys.ACCESS_TOKEN
import com.androidbolts.minitiktok.core.storage.prefs.PrefKeys.PREFS_NAME
import com.androidbolts.minitiktok.core.utils.AppConstants
import com.google.gson.Gson
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrefsDataStoreManagerImpl @Inject constructor(
    private val context: Context
) : PrefsDataStoreManager {
    private val Context.prefsDataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)
    private val gson = Gson()

    override fun getAccessToken(): Flow<String?> {
        return context.prefsDataStore.data.map {
            it[ACCESS_TOKEN] ?: AppConstants.EMPTY_STRING
        }
    }

    override suspend fun setAccessToken(accessToken: String) {
        context.prefsDataStore.edit {
            it[ACCESS_TOKEN] = accessToken
        }
    }

    override suspend fun clear() {
        context.prefsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}