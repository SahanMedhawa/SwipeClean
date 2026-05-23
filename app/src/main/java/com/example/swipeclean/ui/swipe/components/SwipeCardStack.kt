package com.example.swipeclean.ui.swipe.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction

/**
 * Renders up to three cards stacked back-to-front. Only the top card receives
 * gestures; the lower cards are static placeholders that scale up as the
 * deck advances.
 */
@Composable
fun SwipeCardStack(
    queue: List<MediaItem>,
    onSwiped: (MediaItem, SwipeAction) -> Unit,
    onThresholdCross: () -> Unit,
    onSnapBack: () -> Unit,
    onCardTapped: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val visible = queue.take(3)
        // Render bottom-up so the topmost card is the last one composed
        // (and therefore highest in z-order).
        visible.indices.reversed().forEach { idx ->
            val item = visible[idx]
            AnimatedContent(
                targetState = item.id,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) + scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(220)
                    ) togetherWith fadeOut(animationSpec = tween(120))
                },
                label = "card-$idx",
                modifier = Modifier.fillMaxSize()
            ) { _ ->
                SwipeCard(
                    item = item,
                    isTop = idx == 0,
                    stackIndex = idx,
                    onSwiped = { action -> onSwiped(item, action) },
                    onThresholdCross = onThresholdCross,
                    onSnapBack = onSnapBack,
                    onTap = { if (idx == 0) onCardTapped(item) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                )
            }
        }
    }
}
