package com.androidbolts.minitiktok.core.common

import com.androidbolts.minitiktok.features.auth.domain.model.User
import com.androidbolts.minitiktok.features.feed.domain.model.Feed
import com.google.gson.annotations.SerializedName

data class FeedDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("author")
    val author: UserDto,
    @SerializedName("media_url")
    val mediaUrl: String,
    @SerializedName("media_type")
    val mediaType: String,

    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("caption")
    val caption: String,
    @SerializedName("sound_name")
    val soundName: String,
    @SerializedName("like_count")
    val likeCount: String,
    @SerializedName("comment_count")
    val commentCount: String,
    @SerializedName("share_count")
    val shareCount: String,
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("sound_avatar_url")
    val soundAvatarUrl: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
) {
    fun toDomain(): Feed {
        return Feed(
            id = id,
            title = title,
            author = author.toDomain(),
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            isVerified = isVerified,
            caption = caption,
            soundName = soundName,
            likeCount = likeCount,
            commentCount = commentCount,
            shareCount = shareCount,
            avatarUrl = avatarUrl,
            soundAvatarUrl = soundAvatarUrl,
            date = createdAt,
        )
    }
}