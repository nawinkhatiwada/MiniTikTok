package com.androidbolts.minitiktok.features.auth.data

import com.androidbolts.minitiktok.core.common.UserDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): UserDto
}