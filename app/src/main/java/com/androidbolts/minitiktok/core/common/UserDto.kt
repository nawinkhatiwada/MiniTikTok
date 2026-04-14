package com.androidbolts.minitiktok.core.common

import com.androidbolts.minitiktok.features.auth.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val displayName: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("avatar_url")
    val avatarUrl: String,
) {
    fun toDomain(): User {
        return User(
            id = id,
            displayName = displayName,
            username = username,
            email = email,
            avatarUrl = avatarUrl
        )
    }
}