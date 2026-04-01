package com.androidbolts.minitiktok

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    object FeedScreen: Screen()
}
