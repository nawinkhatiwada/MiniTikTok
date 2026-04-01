package com.androidbolts.minitiktok.features.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FeedScreen(
    uiState: FeedUiState,
    onTriggerUserEvent: (FeedUserEvent) -> Unit
) {
    val pagerState = rememberPagerState(
        pageCount = { uiState.videos.size }
    )

    Box(modifier = Modifier.fillMaxSize()) {

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { uiState.videos[it].id }
        ) { page ->
            val video = uiState.videos[page]

            LaunchedEffect(page) {
                if (page >= uiState.videos.size - 2 && !uiState.isLoadingMore) {
                    onTriggerUserEvent(FeedUserEvent.OnLoadMoreFeed)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                VideoItem(
                    videoUrl = video.mediaUrl,
                    isVisible = pagerState.currentPage == page
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                FeedBottomInfo(
                    video = video,
                    modifier = Modifier.align(Alignment.BottomStart)
                )

                FeedActionSidebar(
                    video = video,
                    onLike = { onTriggerUserEvent(FeedUserEvent.OnLikeClicked(video.id)) },
                    onComment = { onTriggerUserEvent(FeedUserEvent.OnCommentClicked(video.id)) },
                    onShare = { onTriggerUserEvent(FeedUserEvent.OnShareClicked(video.id)) },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        FeedTopBar(
            selected = uiState.feedType,
            onSelect = { type ->
                onTriggerUserEvent(FeedUserEvent.OnFeedTypeChanged(feedType = type))
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    }
}