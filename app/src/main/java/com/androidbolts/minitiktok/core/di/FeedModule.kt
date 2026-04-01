package com.androidbolts.minitiktok.core.di

import com.androidbolts.minitiktok.BuildConfig
import com.androidbolts.minitiktok.features.feed.data.FeedApi
import com.androidbolts.minitiktok.features.feed.data.fake.FakeFeedApi
import com.androidbolts.minitiktok.features.feed.domain.FeedRepository
import com.androidbolts.minitiktok.features.feed.domain.FeedRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {
    @Provides
    @Singleton
    fun provideFeedApi(retrofit: Retrofit, fakeFeedApi: FakeFeedApi): FeedApi =
        // ignore this warning
        if(BuildConfig.USE_FAKE_DATA) {
            fakeFeedApi
        } else{
            retrofit.create(FeedApi::class.java)
        }

    @Provides
    @Singleton
    fun provideFeedRepository(api: FeedApi): FeedRepository =
        FeedRepositoryImpl(api)
}