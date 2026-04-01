package com.androidbolts.minitiktok.core.di

import com.androidbolts.minitiktok.core.utils.stringprovider.StringProvider
import com.androidbolts.minitiktok.features.auth.data.AuthApi
import com.androidbolts.minitiktok.features.auth.domain.AuthRepository
import com.androidbolts.minitiktok.features.auth.domain.AuthRepositoryImpl
import com.androidbolts.minitiktok.features.auth.domain.usecase.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository =
        AuthRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideLoginUseCase(repository: AuthRepository, stringProvider: StringProvider) =
        LoginUseCase(repository, stringProvider)
}