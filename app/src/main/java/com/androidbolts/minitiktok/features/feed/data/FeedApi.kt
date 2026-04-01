package com.androidbolts.minitiktok.features.feed.data

import com.androidbolts.minitiktok.core.common.FeedDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedApi {
    @GET("feed/for-you")
    suspend fun forYouList(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): List<FeedDto>

    @GET("feed/following")
    suspend fun getFollowingList(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): List<FeedDto>
}