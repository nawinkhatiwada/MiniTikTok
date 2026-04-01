package com.androidbolts.minitiktok.features.feed.domain

import com.androidbolts.minitiktok.core.common.enums.FeedType
import com.androidbolts.minitiktok.core.utils.ResultType
import com.androidbolts.minitiktok.features.feed.domain.model.Feed

interface FeedRepository {
    suspend fun getFeed(page: Int, type: FeedType): ResultType<List<Feed>>
}