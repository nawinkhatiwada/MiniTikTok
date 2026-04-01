package com.androidbolts.minitiktok.features.feed.domain.usecase

import com.androidbolts.minitiktok.core.common.enums.FeedType
import com.androidbolts.minitiktok.core.utils.ResultType
import com.androidbolts.minitiktok.features.feed.domain.FeedRepository
import com.androidbolts.minitiktok.features.feed.domain.model.Feed
import jakarta.inject.Inject

class FeedUseCase @Inject constructor(
    private val repository: FeedRepository
) {
    suspend operator fun invoke(
        page: Int,
        type: FeedType
    ): ResultType<List<Feed>> {
        return repository.getFeed(page, type)
    }
}