package com.androidbolts.minitiktok.features.auth.domain.model

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val username: String,
    val avatarUrl: String
)