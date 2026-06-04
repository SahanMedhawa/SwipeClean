package com.example.swipeclean.ui.swipe.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.swipeclean.R
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.ui.theme.ActionBinOverlay
import com.example.swipeclean.ui.theme.ActionFaveOverlay
import com.example.swipeclean.ui.theme.ActionKeepOverlay
import com.example.swipeclean.util.ByteFormat
import kotlin.math.abs
import kotlinx.coroutines.launch

private enum class DragAxis { HORIZONTAL, VERTICAL }

@Composable
fun SwipeCard(
    item: MediaItem,
    isTop: Boolean,
    stackIndex: Int = 0,
    autoplayVideos: Boolean = true,
    muteVideosByDefault: Boolean = true,
    showSwipeActionBar: Boolean = true,
    onSwiped: (SwipeAction) -> Unit,
    onThresholdCross: () -> Unit,
    onSnapBack: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val thresholdPx = screenWidthPx * 0.025f
    val arcHeightPx = with(density) { 150.dp.toPx() }

    val haptic = LocalHapticFeedback.current

    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var thresholdNotified by remember(item.id) { mutableStateOf(false) }
    var dismissed by remember(item.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var dragAxis by remember { mutableStateOf<DragAxis?>(null) }
    var accumulatedX by remember { mutableStateOf(0f) }
    var accumulatedY by remember { mutableStateOf(0f) }

    var isMuted by remember(item.id) { mutableStateOf(muteVideosByDefault) }

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

    val baseRotation = if (isTop) rotation else 0f
    val cardScale = if (isTop) 1f - 0.04f * dragMagnitude else 1f
    val cardAlpha = if (isTop) 1f - 0.4f * (dragMagnitude.coerceAtLeast(verticalMagnitude * 0.6f)) else 1f

    // When top card is pulled, we animate its radius slightly so it "lifts" off the screen
    // Background cards have a rounded shape to look like a stack.
    val animatedRadius = if (isTop) (dragMagnitude * 32).coerceIn(0f, 32f).dp else 32.dp
    val cardShape = RoundedCornerShape(animatedRadius)

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = if (isTop) dragX else 0f
                translationY = if (isTop) dragY else 0f
                rotationZ = baseRotation
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
                shadowElevation = if (dragMagnitude > 0f) 24f else 0f
                shape = cardShape
                clip = true
                ambientShadowColor = Color.Black.copy(alpha = 0.6f)
                spotShadowColor = Color.Black.copy(alpha = 0.5f)
            }
            .clip(cardShape)
            .pointerInput(item.id, isTop) {
                if (!isTop) return@pointerInput
                detectTapGestures(onTap = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (item.isVideo) {
                        isMuted = !isMuted
                    } else {
                        onTap()
                    }
                })
            }
            .pointerInput(item.id, isTop) {
                if (!isTop) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        accumulatedX = 0f
                        accumulatedY = 0f
                        dragAxis = null
                    },
                    onDragEnd = {
                        val current = offset.value
                        val crossedHorizontal = abs(current.x) >= thresholdPx
                        val crossedVertical = -current.y >= thresholdPx

                        val action = when {
                            dragAxis == DragAxis.VERTICAL && crossedVertical -> SwipeAction.FAVORITE
                            dragAxis == DragAxis.HORIZONTAL && crossedHorizontal && current.x < 0 -> SwipeAction.BIN
                            dragAxis == DragAxis.HORIZONTAL && crossedHorizontal && current.x > 0 -> SwipeAction.KEEP
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
                                offset.animateTo(target, animationSpec = tween(durationMillis = 350))
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
                    },
                    onDragCancel = {
                        scope.launch {
                            offset.animateTo(Offset.Zero, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                            onSnapBack()
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedX += dragAmount.x
                        accumulatedY += dragAmount.y

                        if (dragAxis == null) {
                            if (abs(accumulatedX) > 10f || abs(accumulatedY) > 10f) {
                                dragAxis = if (abs(accumulatedX) > abs(accumulatedY)) DragAxis.HORIZONTAL else DragAxis.VERTICAL
                            }
                        }

                        if (dragAxis == DragAxis.HORIZONTAL) {
                            val normX = (accumulatedX / screenWidthPx).coerceIn(-1.5f, 1.5f)
                            val newY = (normX * normX) * arcHeightPx
                            scope.launch { offset.snapTo(Offset(accumulatedX, newY)) }
                        } else if (dragAxis == DragAxis.VERTICAL) {
                            if (accumulatedY < 0) {
                                scope.launch { offset.snapTo(Offset(0f, accumulatedY)) }
                            } else {
                                scope.launch { offset.snapTo(Offset(0f, 0f)) }
                            }
                        }

                        val mag = (abs(offset.value.x) / thresholdPx).coerceIn(0f, 1f)
                        val verticalMag = (-offset.value.y / thresholdPx).coerceIn(0f, 1f)
                        val pastThreshold = mag >= 1f || verticalMag >= 1f
                        if (pastThreshold && !thresholdNotified) {
                            thresholdNotified = true
                            onThresholdCross()
                        } else if (!pastThreshold) {
                            thresholdNotified = false
                        }
                    }
                )
            }
    ) {
        CardSurface(
            item = item,
            isTop = isTop,
            autoplayVideos = autoplayVideos,
            isMuted = isMuted,
            onMuteToggle = { 
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isMuted = !isMuted 
            },
            showSwipeActionBar = showSwipeActionBar
        )
        
        // Full screen color tint based on swipe direction, like the HTML sepia/hue-rotate
        if (isTop && dragMagnitude > 0f && dragAxis == DragAxis.HORIZONTAL) {
            val overlayColor = if (dragX < 0) {
                Color.Red.copy(alpha = dragMagnitude * 0.15f)
            } else {
                Color.Green.copy(alpha = dragMagnitude * 0.15f)
            }
            Box(modifier = Modifier.fillMaxSize().background(overlayColor))
        }
    }
}

@Composable
private fun CardSurface(
    item: MediaItem,
    isTop: Boolean,
    autoplayVideos: Boolean,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    showSwipeActionBar: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (item.isVideo && isTop) {
            VideoCardPlayer(
                uri = item.uri,
                isActive = true,
                isMuted = isMuted,
                autoPlay = autoplayVideos
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = item.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
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
        }

        // Bottom scrim gradient for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x33000000), // black/20
                            Color(0x80000000), // black/50
                            Color(0xCC000000), // black/80
                            Color(0xE6000000)  // black/90
                        )
                    )
                )
        )
        
        // Metadata text placed securely above the action bar
        CardMetadata(
            item = item,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (showSwipeActionBar) 220.dp else 48.dp)
        )

        // Mute Icon at Top Right for videos
        if (item.isVideo && isTop) {
            IconButton(
                onClick = onMuteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 100.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = "Toggle Mute",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun CardMetadata(item: MediaItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = item.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.graphicsLayer {
                shadowElevation = 4f
                ambientShadowColor = Color.Black
                spotShadowColor = Color.Black
            }
        )
        
        val details = buildString {
            append(ByteFormat.format(item.sizeBytes))
            append(" • ")
            append(item.resolutionLabel)
            item.durationMs?.let {
                append(" • ")
                append(formatDuration(it))
            }
            append("\n")
            append(item.folderName)
        }
        
        Text(
            text = details,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                shadowElevation = 4f
                ambientShadowColor = Color.Black
                spotShadowColor = Color.Black
            }
        )
    }
}

@Composable
private fun BoxScope.SwipeLabel(
    text: String,
    icon: ImageVector,
    badgeColor: Color,
    textColor: Color,
    alpha: Float,
    alignment: Alignment,
    labelRotation: Float
) {
    AnimatedVisibility(
        visible = alpha > 0.05f,
        enter = fadeIn(tween(150)) + scaleIn(
            initialScale = 0.6f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.6f),
        modifier = Modifier
            .align(alignment)
            .padding(24.dp)
            .graphicsLayer { rotationZ = labelRotation }
    ) {
        Surface(
            color = badgeColor,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = textColor
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
