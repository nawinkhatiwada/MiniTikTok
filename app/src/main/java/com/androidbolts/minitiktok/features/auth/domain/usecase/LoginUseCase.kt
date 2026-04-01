package com.androidbolts.minitiktok.features.auth.domain.usecase

import android.util.Patterns
import com.androidbolts.minitiktok.core.utils.ResultType
import com.androidbolts.minitiktok.core.utils.stringprovider.StringProvider
import com.androidbolts.minitiktok.features.auth.domain.AuthRepository
import com.androidbolts.minitiktok.features.auth.domain.model.User
import jakarta.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val stringProvider: StringProvider
) {
    suspend operator fun invoke(email: String, password: String): ResultType<User> {
        if (email.isBlank()) {
            return ResultType.Error("Email cannot be empty")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ResultType.Error("Invalid email format")
        }

        if (password.isBlank()) {
            return ResultType.Error("Password cannot be empty")
        }

        if (password.length < 6) {
            return ResultType.Error("Password must be at least 6 characters")
        }

        return repository.login(email, password)
    }
}