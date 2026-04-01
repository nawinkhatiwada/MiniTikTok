package com.androidbolts.minitiktok.features.auth.domain

import com.androidbolts.minitiktok.core.utils.ResultType
import com.androidbolts.minitiktok.core.utils.safeApiCall
import com.androidbolts.minitiktok.features.auth.data.AuthApi
import com.androidbolts.minitiktok.features.auth.data.LoginRequest
import com.androidbolts.minitiktok.features.auth.domain.model.User
import jakarta.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {

     override suspend fun login(email: String, password: String): ResultType<User> {
        return safeApiCall {
            val response = api.login(LoginRequest(email, password))
            response.toDomain()
        }
    }
}