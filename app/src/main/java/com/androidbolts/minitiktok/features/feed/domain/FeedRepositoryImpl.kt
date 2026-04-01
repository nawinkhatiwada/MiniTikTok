package com.androidbolts.minitiktok.features.feed.domain

import com.androidbolts.minitiktok.core.common.enums.FeedType
import com.androidbolts.minitiktok.core.utils.ResultType
import com.androidbolts.minitiktok.features.feed.data.FeedApi
import com.androidbolts.minitiktok.features.feed.domain.model.Feed
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val api: FeedApi
) : FeedRepository {

    override suspend fun getFeed(
        page: Int, type: FeedType
    ): ResultType<List<Feed>> {
        return try {
            val response = when (type) {
                FeedType.FOR_YOU -> api.forYouList(page)
                FeedType.FOLLOWING -> api.getFollowingList(page)
            }
            ResultType.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            ResultType.Error(e.message ?: "Unknown error occurred")
        }
    }
}