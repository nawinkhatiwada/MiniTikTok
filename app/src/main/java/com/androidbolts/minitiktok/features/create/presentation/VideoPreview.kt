package com.androidbolts.minitiktok.features.create.presentation

import android.net.Uri
import android.util.Log
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
@Composable
fun VideoPreview(
    modifier: Modifier = Modifier,
    uri: Uri,
    isFrontCamera: Boolean = false
) {
    val context = LocalContext.current

    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d("VideoPreview", "state=$playbackState")
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d("VideoPreview", "isPlaying=$isPlaying")
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e("VideoPreview", "player error", error)
                }

                override fun onRenderedFirstFrame() {
                    Log.d("VideoPreview", "first frame rendered")
                }
            })
        }
    }

    LaunchedEffect(player, uri) {
        Log.d("VideoPreview", "uri=$uri")
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    DisposableEffect(player) {
        onDispose {
            player.clearVideoSurface()
            player.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            TextureView(ctx)
        },
        update = { textureView ->
            player.setVideoTextureView(textureView)
        },
        modifier = modifier.graphicsLayer {
            scaleX = if (isFrontCamera) -1f else 1f
        }
    )
}