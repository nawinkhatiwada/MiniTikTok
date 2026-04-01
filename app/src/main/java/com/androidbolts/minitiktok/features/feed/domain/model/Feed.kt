package com.androidbolts.minitiktok.features.feed.domain.model

import com.androidbolts.minitiktok.features.auth.domain.model.User

data class Feed(
    val id: String,
    val title: String,
    val author: User,
    val mediaUrl: String,
    val mediaType: String,
    val isVerified: Boolean = false,
    val caption: String,
    val soundName: String,
    val likeCount: String,
    val commentCount: String,
    val shareCount: String,
    val avatarUrl: String,
    val soundAvatarUrl: String,
    val date: String
)