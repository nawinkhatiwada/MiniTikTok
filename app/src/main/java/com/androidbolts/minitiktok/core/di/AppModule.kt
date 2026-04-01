package com.androidbolts.minitiktok.core.di

import android.content.Context
import com.androidbolts.minitiktok.MiniTiktokApp
import com.androidbolts.minitiktok.core.utils.stringprovider.StringProvider
import com.androidbolts.minitiktok.core.utils.stringprovider.StringProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplication(@ApplicationContext app: Context): MiniTiktokApp {
        return app as MiniTiktokApp
    }

    @Provides
    @Singleton
    fun provideContext(application: MiniTiktokApp): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideStringProvider(context: Context): StringProvider = StringProviderImpl(context)

}