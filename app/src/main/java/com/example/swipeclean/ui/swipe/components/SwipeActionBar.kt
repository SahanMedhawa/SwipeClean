package com.example.swipeclean.ui.swipe.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.swipeclean.R
import com.example.swipeclean.haptics.rememberTapHaptic
import kotlinx.coroutines.launch

@Composable
fun SwipeActionBar(
    onUndo: () -> Unit,
    onBin: () -> Unit,
    onKeep: () -> Unit,
    onFavorite: () -> Unit, // Re-mapped to Keep, but kept for interface compatibility
    canUndo: Boolean,
    modifier: Modifier = Modifier
) {
    val tap = rememberTapHaptic()

    val containerShape = RoundedCornerShape(40.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(24.dp, containerShape)
                .background(Color.White.copy(alpha = 0.1f), containerShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), containerShape)
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rewind
            MinimalActionButton(
                icon = Icons.Rounded.Replay,
                contentDescription = stringResource(R.string.swipe_action_undo),
                tint = Color.White.copy(alpha = 0.7f),
                backgroundColor = Color.Transparent,
                size = 56.dp,
                iconSize = 28.dp,
                enabled = canUndo,
                onClick = {
                    tap()
                    onUndo()
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bin Action
                MinimalActionButton(
                    icon = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.swipe_action_bin),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(28.dp),
                    size = 80.dp,
                    iconSize = 36.dp,
                    hasShadow = true,
                    onClick = {
                        tap()
                        onBin()
                    }
                )
                // Keep Action
                MinimalActionButton(
                    icon = Icons.Rounded.Favorite,
                    contentDescription = stringResource(R.string.swipe_action_keep),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape,
                    size = 80.dp,
                    iconSize = 36.dp,
                    hasShadow = true,
                    onClick = {
                        tap()
                        onKeep()
                    }
                )
            }

            // More Options
            MinimalActionButton(
                icon = Icons.Rounded.MoreVert,
                contentDescription = "More options",
                tint = Color.White.copy(alpha = 0.7f),
                backgroundColor = Color.Transparent,
                size = 56.dp,
                iconSize = 28.dp,
                onClick = {
                    tap()
                    // No-op for now
                }
            )
        }
    }
}

@Composable
private fun MinimalActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    backgroundColor: Color,
    size: Dp,
    iconSize: Dp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    enabled: Boolean = true,
    hasShadow: Boolean = false,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pressScale = remember { Animatable(1f) }

    Box(
        modifier = Modifier
            .size(size)
            .then(if (hasShadow && enabled) Modifier.shadow(8.dp, shape) else Modifier)
            .clip(shape)
            .background(
                color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.35f),
                shape = shape
            )
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
            }
            .pointerInput(enabled, onClick) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        scope.launch {
                            pressScale.animateTo(
                                0.90f,
                                animationSpec = spring(stiffness = 2000f)
                            )
                        }
                        tryAwaitRelease()
                        scope.launch {
                            pressScale.animateTo(
                                1f,
                                animationSpec = spring(
                                    dampingRatio = 0.4f,
                                    stiffness = 600f
                                )
                            )
                        }
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) tint else tint.copy(alpha = 0.5f)
        )
    }
}
