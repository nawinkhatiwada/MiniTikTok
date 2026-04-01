package com.androidbolts.minitiktok.core.di

import android.content.Context
import com.androidbolts.minitiktok.core.storage.prefs.PrefsDataStoreManager
import com.androidbolts.minitiktok.core.storage.prefs.PrefsDataStoreManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Singleton
    @Provides
    fun providePrefsDataStoreManager(
        @ApplicationContext context: Context
    ): PrefsDataStoreManager =
        PrefsDataStoreManagerImpl(context)
}