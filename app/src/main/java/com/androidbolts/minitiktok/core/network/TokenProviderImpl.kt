package com.androidbolts.minitiktok.core.network

import com.androidbolts.minitiktok.core.storage.prefs.PrefsDataStoreManager
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class TokenProviderImpl @Inject constructor(
    private val dataStoreManager: PrefsDataStoreManager
) : TokenProvider {
    override suspend fun getToken(): String? {
        return  dataStoreManager.getAccessToken().firstOrNull()
    }

    suspend fun saveToken(newToken: String) {
        dataStoreManager.setAccessToken(newToken)
    }
}