package com.example.swipeclean.ui.swipe

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swipeclean.R
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.ui.swipe.components.SessionCounterChip
import com.example.swipeclean.ui.swipe.components.SwipeActionBar
import com.example.swipeclean.ui.swipe.components.SwipeCardStack
import com.example.swipeclean.ui.swipe.components.UndoChip

@Composable
fun SwipeRoute(
    onBack: () -> Unit,
    onOpenPreview: (Long) -> Unit,
    viewModel: SwipeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SwipeScreen(
        state = state,
        onBack = onBack,
        onSwiped = viewModel::onSwiped,
        onUndo = viewModel::undo,
        onDismissUndo = viewModel::dismissUndo,
        onThresholdCross = viewModel::thresholdCross,
        onSnapBack = viewModel::cardSnappedBack,
        onOpenPreview = onOpenPreview
    )
}

@Composable
private fun SwipeScreen(
    state: SwipeUiState,
    onBack: () -> Unit,
    onSwiped: (MediaItem, SwipeAction) -> Unit,
    onUndo: () -> Unit,
    onDismissUndo: () -> Unit,
    onThresholdCross: () -> Unit,
    onSnapBack: () -> Unit,
    onOpenPreview: (Long) -> Unit
) {
    val baseColor = MaterialTheme.colorScheme.background
    val topItem = state.queue.firstOrNull()
    // Tint hint based on the first card's pending direction (Phase 2 will hook
    // this to live drag offset via a side-channel; for now it's a neutral wash).
    val targetTint = when {
        topItem == null -> baseColor
        else -> baseColor
    }
    val tint by animateColorAsState(targetTint, label = "screen-tint")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tint)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopBar(
                reviewedCount = state.reviewedCount,
                binBytes = state.binBytesThisSession,
                onBack = onBack
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.empty && state.queue.isEmpty()) {
                    EmptyState()
                } else {
                    SwipeCardStack(
                        queue = state.queue,
                        onSwiped = onSwiped,
                        onThresholdCross = onThresholdCross,
                        onSnapBack = onSnapBack,
                        onCardTapped = { item -> onOpenPreview(item.id) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            SwipeActionBar(
                onUndo = onUndo,
                onBin = {
                    val item = state.queue.firstOrNull() ?: return@SwipeActionBar
                    onSwiped(item, SwipeAction.BIN)
                },
                onKeep = {
                    val item = state.queue.firstOrNull() ?: return@SwipeActionBar
                    onSwiped(item, SwipeAction.KEEP)
                },
                onFavorite = {
                    val item = state.queue.firstOrNull() ?: return@SwipeActionBar
                    onSwiped(item, SwipeAction.FAVORITE)
                },
                canUndo = state.reviewedCount > 0
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        UndoChip(
            visible = state.undoVisible,
            token = state.undoToken,
            onUndo = onUndo,
            onTimeout = onDismissUndo,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
        )
    }
}

@Composable
private fun TopBar(
    reviewedCount: Int,
    binBytes: Long,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        SessionCounterChip(
            reviewedCount = reviewedCount,
            binBytes = binBytes,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.swipe_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.swipe_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
