package com.example.swipeclean.ui.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swipeclean.R
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.ui.swipe.components.SessionCounterChip
import com.example.swipeclean.ui.swipe.components.SwipeActionBar
import com.example.swipeclean.ui.swipe.components.SwipeCardStack
import com.example.swipeclean.ui.swipe.components.UndoChip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.swipeclean.ui.theme.ActionKeep

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {


        if (state.empty && state.queue.isEmpty()) {
            EmptyState()
        } else {
            SwipeCardStack(
                queue = state.queue,
                autoplayVideos = state.autoplayVideos,
                muteVideosByDefault = state.muteVideosByDefault,
                showSwipeActionBar = state.showSwipeActionBar,
                onSwiped = onSwiped,
                onThresholdCross = onThresholdCross,
                onSnapBack = onSnapBack,
                onCardTapped = { item -> onOpenPreview(item.id) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gesture Hints (Overlay layer)
        if (!state.empty && state.queue.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "BIN",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "KEEP",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                    )
                }
            }
        }

        // Top Navigation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            TopBar(
                onBack = onBack,
                reviewedCount = state.reviewedCount,
                binBytes = state.binBytesThisSession,
                firstItem = state.queue.firstOrNull(),
                onOpenPreview = onOpenPreview
            )
            Text(
                text = "${state.queue.size} items remaining",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
        }

        if (state.showSwipeActionBar) {
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
                canUndo = state.reviewedCount > 0,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }

        UndoChip(
            visible = state.undoVisible,
            token = state.undoToken,
            onUndo = onUndo,
            onTimeout = onDismissUndo,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (state.showSwipeActionBar) 140.dp else 80.dp)
        )
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    reviewedCount: Int,
    binBytes: Long,
    firstItem: MediaItem?,
    onOpenPreview: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = Color.White
            )
        }
        
        SessionCounterChip(
            reviewedCount = reviewedCount,
            binBytes = binBytes,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                .clip(CircleShape)
                .clickable(enabled = firstItem != null) {
                    firstItem?.let { onOpenPreview(it.id) }
                },
            contentAlignment = Alignment.Center
        ) {
            if (firstItem != null) {
                AsyncImage(
                    model = firstItem.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = ActionKeep,
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
