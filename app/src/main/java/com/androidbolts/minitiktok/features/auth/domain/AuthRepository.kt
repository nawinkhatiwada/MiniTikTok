package com.androidbolts.minitiktok.features.auth.domain

import com.androidbolts.minitiktok.core.utils.ResultType
import com.androidbolts.minitiktok.features.auth.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): ResultType<User>
}