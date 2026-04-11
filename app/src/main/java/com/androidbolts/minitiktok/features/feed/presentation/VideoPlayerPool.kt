package com.androidbolts.minitiktok.features.feed.presentation

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import java.io.File

@OptIn(UnstableApi::class)
class VideoPlayerPool(
    private val context: Context,
    poolSize: Int = 5
) {
    companion object {
        private const val CACHE_SIZE_BYTES = 150L * 1024 * 1024
        private const val CACHE_DIR_NAME   = "video_cache"

        @Volatile private var sharedCache: SimpleCache? = null

        private fun getCache(context: Context): SimpleCache =
            sharedCache ?: synchronized(this) {
                sharedCache ?: SimpleCache(
                    File(context.cacheDir, CACHE_DIR_NAME),
                    LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES)
                ).also { sharedCache = it }
            }
    }

    // ── Cache ─────────────────────────────────────────────────────────────────

    private val cacheDataSourceFactory: DataSource.Factory =
        CacheDataSource.Factory()
            .setCache(getCache(context))
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    /**
     * Persistent URL → resize-mode cache.
     * Survives page eviction so we never calculate the same video's aspect twice.
     */
    private val urlResizeModeCache = HashMap<String, Int>()

    // ── Compose-observable maps (read in Composable lambdas) ──────────────────

    /** page → ExoPlayer. Reading inside a composable triggers recompose on change. */
    val pagePlayerMap: SnapshotStateMap<Int, ExoPlayer> = mutableStateMapOf()

    /**
     * page → RESIZE_MODE_ZOOM / RESIZE_MODE_FIT.
     * Set from cache before the player attaches so the PlayerView is already
     * the right size when the first frame arrives — no visible snap.
     * Default is ZOOM (fills screen) so portrait content is always correct on first play.
     */
    val pageResizeModeMap: SnapshotStateMap<Int, Int> = mutableStateMapOf()

    // ── Internal bookkeeping ──────────────────────────────────────────────────

    private val pageToPlayerIdx = mutableMapOf<Int, Int>()
    private val playerToPage    = mutableMapOf<Int, Int>()
    private val playerUrls      = arrayOfNulls<String>(poolSize)

    // ── Players ───────────────────────────────────────────────────────────────

    private val players: List<ExoPlayer> = List(poolSize) { idx ->
        createPlayer().also { player ->
            // Each player carries a lightweight listener that fires exactly once per
            // URL to capture & cache the true display aspect ratio.
            player.addListener(object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    onPlayerVideoSizeChanged(idx, videoSize)
                }
            })
        }
    }

    private fun createPlayer(): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(5_000, 15_000, 1_500, 3_000)
            .build()

        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build()
            .apply {
                repeatMode    = Player.REPEAT_MODE_ONE
                volume        = 0f
                playWhenReady = false
            }
    }

    /**
     * Called by each player's listener when the video dimensions are first known.
     * Accounts for rotation metadata and non-square pixels so the result is the
     * true *display* aspect ratio — not the coded storage dimensions.
     * Result is cached by URL and pushed to the reactive map for the owning page.
     */
    private fun onPlayerVideoSizeChanged(playerIdx: Int, videoSize: VideoSize) {
        if (videoSize.width == 0 || videoSize.height == 0) return
        val url = playerUrls[playerIdx] ?: return

        // If we already know this URL's mode, nothing to compute
        if (urlResizeModeCache.containsKey(url)) return

        // unappliedRotationDegrees: rotation the codec/surface has NOT yet applied.
        // For 90°/270° the stored width/height are swapped relative to display.
        val unapplied  = videoSize.unappliedRotationDegrees
        val isRotated  = unapplied == 90 || unapplied == 270

        // pixelWidthHeightRatio: SAR (sample aspect ratio). Usually 1.0 for modern content.
        val displayAspect = if (isRotated) {
            videoSize.height.toFloat() * videoSize.pixelWidthHeightRatio / videoSize.width.toFloat()
        } else {
            videoSize.width.toFloat() * videoSize.pixelWidthHeightRatio / videoSize.height.toFloat()
        }

        val mode = if (displayAspect > 1f) {
            // Landscape: fit inside screen so the full frame is visible (letterbox)
            AspectRatioFrameLayout.RESIZE_MODE_FIT
        } else {
            // Portrait / square: zoom to fill screen (tiny side-crop at most)
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }

        urlResizeModeCache[url] = mode

        // Push to Compose immediately so VideoItem resizes before first frame
        playerToPage[playerIdx]?.let { page ->
            pageResizeModeMap[page] = mode
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Rotate the pool window to keep [currentPage±2] each assigned to a player.
     * Evicts pages outside the window, assigns freed players to new pages, and
     * applies any cached resize mode immediately (before the player attaches).
     */
    fun onPageSettled(currentPage: Int, videoUrls: Map<Int, String>) {
        val wanted = (-2..2)
            .map { currentPage + it }
            .filter { videoUrls.containsKey(it) }

        // ── Evict pages outside the window ────────────────────────────────────
        val freed = mutableListOf<Int>()
        pageToPlayerIdx.keys.toList().forEach { page ->
            if (page !in wanted) {
                val idx = pageToPlayerIdx.remove(page) ?: return@forEach
                playerToPage.remove(idx)
                pagePlayerMap.remove(page)
                freed.add(idx)
            }
        }
        // Collect fully unassigned players too
        for (i in players.indices) {
            if (i !in playerToPage && i !in freed) freed.add(i)
        }

        // ── Assign players to pages that need one ─────────────────────────────
        for (page in wanted) {
            if (pageToPlayerIdx.containsKey(page)) {
                // Already assigned — verify URL is current
                val idx = pageToPlayerIdx[page]!!
                val url = videoUrls[page] ?: continue
                prepareIfNeeded(idx, url)
                continue
            }

            val idx = freed.removeFirstOrNull() ?: continue
            val url = videoUrls[page] ?: continue

            pageToPlayerIdx[page] = idx
            playerToPage[idx]     = page
            prepareIfNeeded(idx, url)
            pagePlayerMap[page]   = players[idx]
        }
    }

    /** Start audible playback for [page]. Pauses + mutes all others. */
    fun play(page: Int) {
        val currentIdx = pageToPlayerIdx[page] ?: return
        pageToPlayerIdx.forEach { (p, idx) ->
            if (p != page) { players[idx].pause(); players[idx].volume = 0f }
        }
        players[currentIdx].volume        = 1f
        players[currentIdx].playWhenReady = true
        players[currentIdx].play()
    }

    fun pause(page: Int) {
        pageToPlayerIdx[page]?.let { players[it].pause() }
    }

    fun pauseAll() {
        players.forEach { it.pause(); it.playWhenReady = false; it.volume = 0f }
    }

    fun release() {
        players.forEach { it.release() }
        pagePlayerMap.clear()
        pageResizeModeMap.clear()
        pageToPlayerIdx.clear()
        playerToPage.clear()
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun prepareIfNeeded(playerIdx: Int, url: String) {
        // Apply the cached resize mode immediately — this runs BEFORE the player
        // is attached to the view, so VideoItem is already sized correctly when
        // the first frame arrives.
        val cachedMode = urlResizeModeCache[url]
        val page = playerToPage[playerIdx]
        if (page != null) {
            pageResizeModeMap[page] = cachedMode ?: AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }

        if (playerUrls[playerIdx] == url) return  // Already buffering/ready — skip

        val player = players[playerIdx]
        player.stop()
        player.clearMediaItems()
        player.volume        = 0f
        player.playWhenReady = false
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        player.prepare()
        playerUrls[playerIdx] = url
    }
}
