package com.example.swipeclean.ui.preview

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as Media3MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Lightweight Media3-backed video surface.
 *
 * The video starts muted and autoplaying, matching the spec. Tapping the
 * PlayerView surface toggles the mute. Lifecycle teardown happens in
 * DisposableEffect.
 */
@Composable
fun VideoPlayer(
    uri: Uri,
    autoplay: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(Media3MediaItem.fromUri(uri))
            volume = 0f
            playWhenReady = autoplay
            prepare()
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                setOnClickListener {
                    player.volume = if (player.volume > 0f) 0f else 1f
                }
            }
        },
        update = { view ->
            view.player = player
        }
    )
}
