package com.androidbolts.minitiktok.features.feed.presentation

import androidx.annotation.OptIn
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun FeedScreen(
    uiState: FeedUiState,
    onTriggerUserEvent: (FeedUserEvent) -> Unit,
    isVisible: Boolean = true
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val pagerState = rememberPagerState(pageCount = { uiState.videos.size })

    val playerPool = remember { VideoPlayerPool(context, poolSize = 5) }

    // isPaused resets to false every time the user lands on a new page
    var isPaused by remember(pagerState.settledPage) { mutableStateOf(false) }

    // ── Visibility: free decoders when an overlay covers the feed ────────────
    // VideoPlayerPool keeps up to 5 prepared ExoPlayers. On decoder-limited devices
    // the captured-video preview can't acquire a decoder while those are held,
    // resulting in a black screen. Stopping them frees the decoders immediately;
    // onPageSettled re-prepares when we return.
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            playerPool.releaseDecoders()
        } else if (uiState.videos.isNotEmpty()) {
            val urlMap = uiState.videos.indices.associate { i -> i to uiState.videos[i].mediaUrl }
            playerPool.onPageSettled(pagerState.settledPage, urlMap)
            if (!isPaused) playerPool.play(pagerState.settledPage)
        }
    }

    // ── Lifecycle: release players when the composable leaves composition ─────
    DisposableEffect(Unit) {
        onDispose { playerPool.release() }
    }

    // ── Lifecycle: pause when app goes to background, resume on foreground ────
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> playerPool.pauseAll()
                Lifecycle.Event.ON_RESUME -> {
                    if (!isPaused) playerPool.play(pagerState.settledPage)
                }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // ── Core: react to page-settle changes ────────────────────────────────────
    // Builds the full URL map and rotates the player pool window.
    LaunchedEffect(pagerState.settledPage) {
        if (uiState.videos.isEmpty()) return@LaunchedEffect
        val page = pagerState.settledPage
        val urlMap = uiState.videos.indices.associate { i -> i to uiState.videos[i].mediaUrl }
        playerPool.onPageSettled(page, urlMap)
        if (!isPaused) playerPool.play(page)
    }

    // ── Core: react to new videos appended (load-more) ────────────────────────
    // Re-runs onPageSettled so the pool learns about newly adjacent URLs and
    // starts pre-buffering them immediately.
    LaunchedEffect(uiState.videos) {
        if (uiState.videos.isEmpty()) return@LaunchedEffect
        val page = pagerState.settledPage
        val urlMap = uiState.videos.indices.associate { i -> i to uiState.videos[i].mediaUrl }
        playerPool.onPageSettled(page, urlMap)
        if (!isPaused) playerPool.play(page)
    }

    // ── Pause / resume toggle ─────────────────────────────────────────────────
    LaunchedEffect(isPaused) {
        val page = pagerState.settledPage
        if (isPaused) playerPool.pause(page) else playerPool.play(page)
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { uiState.videos[it].id },
            beyondViewportPageCount = 2
        ) { page ->
            val video = uiState.videos[page]

            // Pagination trigger: load more when approaching the end
            LaunchedEffect(page, uiState.videos.size, uiState.isLoadingMore) {
                if (page >= uiState.videos.size - 2 && !uiState.isLoadingMore) {
                    onTriggerUserEvent(FeedUserEvent.OnLoadMoreFeed)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {

                VideoItem(
                    player     = playerPool.pagePlayerMap[page],
                    resizeMode = playerPool.pageResizeModeMap[page]
                        ?: AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                    isActive   = pagerState.settledPage == page,
                    isPaused   = isPaused,
                    onTogglePause = { isPaused = !isPaused },
                    modifier   = Modifier.fillMaxSize()
                )

                // Bottom gradient for readability
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
                    onLike    = { onTriggerUserEvent(FeedUserEvent.OnLikeClicked(video.id)) },
                    onComment = { onTriggerUserEvent(FeedUserEvent.OnCommentClicked(video.id)) },
                    onShare   = { onTriggerUserEvent(FeedUserEvent.OnShareClicked(video.id)) },
                    modifier  = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        FeedTopBar(
            selected = uiState.feedType,
            onSelect = { type -> onTriggerUserEvent(FeedUserEvent.OnFeedTypeChanged(feedType = type)) },
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
