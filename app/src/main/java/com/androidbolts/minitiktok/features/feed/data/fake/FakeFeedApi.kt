package com.androidbolts.minitiktok.features.feed.data.fake


import com.androidbolts.minitiktok.core.common.FeedDto
import com.androidbolts.minitiktok.features.feed.data.FeedApi
import javax.inject.Inject

class FakeFeedApi @Inject constructor() : FeedApi {

    override suspend fun forYouList(
        page: Int,
        limit: Int
    ): List<FeedDto> {
        return FakeFeedDataSource.getForYouFeed(page)
    }

    override suspend fun getFollowingList(
        page: Int,
        limit: Int
    ): List<FeedDto> {
        return FakeFeedDataSource.getFollowingFeed(page)
    }
}
