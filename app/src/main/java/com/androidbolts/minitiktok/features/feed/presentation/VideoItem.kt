package com.androidbolts.minitiktok.features.feed.presentation

import android.graphics.Color as AndroidColor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.androidbolts.minitiktok.R

@OptIn(UnstableApi::class)
@Composable
fun VideoItem(
    player: ExoPlayer?,
    resizeMode: Int,                  // Supplied by the pool — already correct for this URL
    isActive: Boolean,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Inflate with surface_type="texture_view" so the player renders into the same
    // compositor layer as Compose. SurfaceView (the default) punches a transparent
    // hole through the window and its video layer sits below the app window, which
    // causes full-screen Compose overlays (CreateScreen, EditScreen) to appear blank
    // when the feed is the active tab beneath them.
    val playerView = remember {
        (LayoutInflater.from(context).inflate(R.layout.view_player, null) as PlayerView).apply {
            controllerAutoShow      = false
            controllerHideOnTouch   = false
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)

            // Hold the last rendered frame when the player is detached.
            // Prevents the black flash when scrolling back to a previously played video.
            setKeepContentOnPlayerReset(true)

            // Transparent before the first frame — no opaque black box while buffering.
            setShutterBackgroundColor(AndroidColor.BLACK)

            // Default: ZOOM fills the screen for portrait content.
            // The pool overrides this before the player attaches once the real
            // aspect ratio is known (or from cache on revisits).
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            hideController()
        }
    }

    // Attach / detach the player when this page becomes active or inactive.
    // keepContentOnPlayerReset keeps the last frame visible on detach.
    DisposableEffect(player, isActive) {
        if (isActive && player != null) {
            playerView.player = player
        } else {
            playerView.player = null
        }
        onDispose {
            playerView.player = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (isActive) onTogglePause()
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory  = { playerView },
            // update runs on the main thread after each recomposition where
            // resizeMode changed — no player re-attach, just a layout pass.
            // Because the pool sets pageResizeModeMap BEFORE pagePlayerMap,
            // the mode is applied before the player is ever bound to this view.
            update   = { view -> view.setResizeMode(resizeMode) },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = isActive && isPaused,
            enter   = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit    = scaleOut(targetScale  = 1.5f) + fadeOut()
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_pause),
                contentDescription = "Paused",
                tint               = Color.White.copy(alpha = 0.85f),
                modifier           = Modifier.size(72.dp)
            )
        }
    }
}
