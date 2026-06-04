package com.example.swipeclean.ui.preview

import android.net.Uri
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Pinch-to-zoom + pan image. The transform is bounded to [1f .. 5f] zoom and
 * the pan is clamped so the user can't drag the image entirely off-screen.
 */
@Composable
fun ZoomableImage(
    uri: Uri,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(uri) {
                detectTransformGestures { _, panChange, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    val scaleRatio = if (scale > 0f) newScale / scale else 1f
                    scale = newScale
                    pan = if (newScale > 1f) {
                        Offset(
                            x = (pan.x + panChange.x) * scaleRatio,
                            y = (pan.y + panChange.y) * scaleRatio
                        )
                    } else {
                        Offset.Zero
                    }
                }
            }
    ) {
        AsyncImage(
            model = uri,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = pan.x,
                    translationY = pan.y
                )
        )
    }
}
