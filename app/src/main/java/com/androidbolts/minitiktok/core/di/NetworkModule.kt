package com.androidbolts.minitiktok.core.di

import com.androidbolts.minitiktok.BuildConfig
import com.androidbolts.minitiktok.core.network.ApiInterceptor
import com.androidbolts.minitiktok.core.network.TokenProvider
import com.androidbolts.minitiktok.core.network.TokenProviderImpl
import com.androidbolts.minitiktok.core.storage.prefs.PrefsDataStoreManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECTION_TIME_OUT = 40L
    private const val READ_TIME_OUT = 40L

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .create()

    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Provides
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        apiInterceptor: ApiInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                addInterceptor(apiInterceptor)
                if (BuildConfig.DEBUG) {
                    addInterceptor(httpLoggingInterceptor)
                    addInterceptor(OkHttpProfilerInterceptor())
                }
                connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            }.build()
    }

    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return  Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

//        return retrofit.create(ApiService::class.java)
    }

    @Provides
    fun provideTokenProvider(
        dataStoreManager: PrefsDataStoreManager
    ): TokenProvider = TokenProviderImpl(dataStoreManager)

}
