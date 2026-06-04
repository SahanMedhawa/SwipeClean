package com.example.swipeclean.ui.swipe.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction

/**
 * Renders up to three cards stacked back-to-front with realistic depth.
 *
 * Card 3 (back):   scale = 0.88, translationY = -24dp, alpha = 0.5
 * Card 2 (middle): scale = 0.94, translationY = -12dp, alpha = 0.75
 * Card 1 (front):  scale = 1.0,  full opacity
 *
 * Only the topmost card receives gestures; lower cards render statically.
 */
@Composable
fun SwipeCardStack(
    queue: List<MediaItem>,
    autoplayVideos: Boolean,
    muteVideosByDefault: Boolean,
    showSwipeActionBar: Boolean,
    onSwiped: (MediaItem, SwipeAction) -> Unit,
    onThresholdCross: () -> Unit,
    onSnapBack: () -> Unit,
    onCardTapped: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Box(modifier = modifier) {
        val visible = queue.take(3)

        // Render bottom-up so the topmost card has highest z-order
        visible.indices.reversed().forEach { idx ->
            val item = visible[idx]
            val stackScale = when (idx) {
                0 -> 1f
                1 -> 0.94f
                else -> 0.88f
            }
            val stackTranslateY = when (idx) {
                0 -> 0f
                1 -> with(density) { (-12).dp.toPx() }
                else -> with(density) { (-24).dp.toPx() }
            }
            val stackAlpha = when (idx) {
                0 -> 1f
                1 -> 0.75f
                else -> 0.5f
            }

            key(item.id) {
                SwipeCard(
                    item = item,
                    isTop = idx == 0,
                    stackIndex = idx,
                    autoplayVideos = autoplayVideos,
                    muteVideosByDefault = muteVideosByDefault,
                    showSwipeActionBar = showSwipeActionBar,
                    onSwiped = { action -> onSwiped(item, action) },
                    onThresholdCross = onThresholdCross,
                    onSnapBack = onSnapBack,
                    onTap = { if (idx == 0) onCardTapped(item) },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = stackScale
                            scaleY = stackScale
                            translationY = stackTranslateY
                            alpha = stackAlpha
                        }
                )
            }
        }
    }
}
