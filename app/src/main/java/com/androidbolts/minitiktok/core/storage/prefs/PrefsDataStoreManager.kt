package com.androidbolts.minitiktok.core.storage.prefs

import kotlinx.coroutines.flow.Flow

interface PrefsDataStoreManager {
    fun getAccessToken(): Flow<String?>
    suspend fun setAccessToken(accessToken: String)
    suspend fun clear()


}