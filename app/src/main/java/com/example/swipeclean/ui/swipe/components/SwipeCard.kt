package com.example.swipeclean.ui.swipe.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.swipeclean.R
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.ui.theme.ActionBin
import com.example.swipeclean.ui.theme.ActionFave
import com.example.swipeclean.ui.theme.ActionKeep
import com.example.swipeclean.util.ByteFormat
import kotlin.math.abs
import kotlinx.coroutines.launch

/**
 * Single swipeable card. Owns its own [Animatable] offset, applies rotation,
 * scale, and alpha based on drag distance, and emits the appropriate
 * [SwipeAction] when the drag crosses the threshold.
 *
 * The card calls [onThresholdCross] once per drag the first time the user
 * crosses 30% of screen width (haptic feedback), and [onSnapBack] when a drag
 * is released below threshold and the card springs back to centre.
 *
 * [isTop] gates gesture input: only the topmost card in the stack receives
 * touches; lower cards render statically for depth.
 */
@Composable
fun SwipeCard(
    item: MediaItem,
    isTop: Boolean,
    stackIndex: Int,
    onSwiped: (SwipeAction) -> Unit,
    onThresholdCross: () -> Unit,
    onSnapBack: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val thresholdPx = screenWidthPx * 0.30f

    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var thresholdNotified by remember(item.id) { mutableStateOf(false) }
    var dismissed by remember(item.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(item.id) {
        offset.snapTo(Offset.Zero)
        thresholdNotified = false
        dismissed = false
    }

    val dragX = offset.value.x
    val dragY = offset.value.y
    val rotation = (dragX / screenWidthPx) * 22f
    val dragMagnitude = (abs(dragX) / thresholdPx).coerceIn(0f, 1f)
    val verticalMagnitude = (-dragY / thresholdPx).coerceIn(0f, 1f)

    val stackDepthScale = 1f - 0.05f * stackIndex
    val stackDepthOffsetY = with(density) { (12 * stackIndex).dp.toPx() }

    val baseRotation = if (isTop) rotation else 0f
    val cardScale = stackDepthScale * (if (isTop) 1f - 0.04f * dragMagnitude else 1f)
    val cardAlpha = if (isTop) 1f - 0.4f * (dragMagnitude.coerceAtLeast(verticalMagnitude * 0.6f)) else 1f

    val binOverlay = if (isTop && dragX < 0) dragMagnitude else 0f
    val keepOverlay = if (isTop && dragX > 0) dragMagnitude else 0f
    val faveOverlay = if (isTop && dragY < 0 && abs(dragY) > abs(dragX)) verticalMagnitude else 0f

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = if (isTop) dragX else 0f
                translationY = stackDepthOffsetY + (if (isTop) dragY else 0f)
                rotationZ = baseRotation
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
            }
            .pointerInput(item.id, isTop) {
                if (!isTop) return@pointerInput
                androidx.compose.foundation.gestures.detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(item.id, isTop) {
                if (!isTop) return@pointerInput
                awaitGesture { delta, releaseVelocity ->
                    val newOffset = Offset(
                        x = offset.value.x + delta.x,
                        y = offset.value.y + delta.y
                    )
                    scope.launch { offset.snapTo(newOffset) }

                    val mag = (abs(newOffset.x) / thresholdPx).coerceIn(0f, 1f)
                    val verticalMag = (-newOffset.y / thresholdPx).coerceIn(0f, 1f)
                    val pastThreshold = mag >= 1f || verticalMag >= 1f
                    if (pastThreshold && !thresholdNotified) {
                        thresholdNotified = true
                        onThresholdCross()
                    } else if (!pastThreshold) {
                        thresholdNotified = false
                    }

                    if (releaseVelocity != null) {
                        val current = offset.value
                        val crossedHorizontal = abs(current.x) >= thresholdPx
                        val crossedVertical = -current.y >= thresholdPx &&
                            abs(current.y) > abs(current.x)

                        val action = when {
                            crossedVertical -> SwipeAction.FAVORITE
                            crossedHorizontal && current.x < 0 -> SwipeAction.BIN
                            crossedHorizontal && current.x > 0 -> SwipeAction.KEEP
                            else -> null
                        }

                        if (action != null && !dismissed) {
                            dismissed = true
                            scope.launch {
                                val target = when (action) {
                                    SwipeAction.BIN -> Offset(-screenWidthPx * 1.5f, current.y)
                                    SwipeAction.KEEP -> Offset(screenWidthPx * 1.5f, current.y)
                                    SwipeAction.FAVORITE -> Offset(current.x, -screenWidthPx * 1.5f)
                                }
                                offset.animateTo(target, animationSpec = tween(durationMillis = 220))
                                onSwiped(action)
                            }
                        } else {
                            scope.launch {
                                offset.animateTo(
                                    targetValue = Offset.Zero,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                onSnapBack()
                            }
                        }
                    }
                }
            }
    ) {
        CardSurface(item = item)

        if (isTop) {
            SwipeLabel(
                text = stringResource(R.string.swipe_label_bin),
                color = ActionBin,
                alpha = binOverlay,
                alignment = Alignment.TopStart
            )
            SwipeLabel(
                text = stringResource(R.string.swipe_label_keep),
                color = ActionKeep,
                alpha = keepOverlay,
                alignment = Alignment.TopEnd
            )
            SwipeLabel(
                text = stringResource(R.string.swipe_label_fave),
                color = ActionFave,
                alpha = faveOverlay,
                alignment = Alignment.TopCenter
            )
        }
    }
}

@Composable
private fun CardSurface(item: MediaItem) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = item.displayName,
                modifier = Modifier.fillMaxSize()
            )
            if (item.isVideo) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(72.dp),
                    tint = Color.White.copy(alpha = 0.92f)
                )
            }
            CardMetadata(item = item, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun CardMetadata(item: MediaItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(PaddingValues(horizontal = 20.dp, vertical = 14.dp))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ByteFormat.format(item.sizeBytes),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text("·", color = Color.White.copy(alpha = 0.6f))
                Text(
                    text = item.resolutionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                item.durationMs?.let { duration ->
                    Text("·", color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            Text(
                text = item.folderName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BoxScope.SwipeLabel(
    text: String,
    color: Color,
    alpha: Float,
    alignment: Alignment
) {
    AnimatedVisibility(
        visible = alpha > 0.05f,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(alignment).padding(24.dp)
    ) {
        Surface(
            color = color.copy(alpha = (alpha * 0.85f).coerceIn(0.4f, 0.95f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

/**
 * Helper extension wrapping detectDragGestures into a coroutine-friendly form.
 * Calls [onDragOrRelease] for every drag delta (with releaseVelocity = null)
 * and once on release (with releaseVelocity set).
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.awaitGesture(
    onDragOrRelease: (delta: Offset, releaseVelocity: Offset?) -> Unit
) {
    androidx.compose.foundation.gestures.detectDragGestures(
        onDrag = { change, dragAmount ->
            change.consume()
            onDragOrRelease(dragAmount, null)
        },
        onDragEnd = { onDragOrRelease(Offset.Zero, Offset.Zero) },
        onDragCancel = { onDragOrRelease(Offset.Zero, Offset.Zero) }
    )
}

