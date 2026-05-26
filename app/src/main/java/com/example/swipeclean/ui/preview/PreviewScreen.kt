package com.example.swipeclean.ui.preview

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swipeclean.R
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.util.ByteFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
@Composable
fun PreviewRoute(
    mediaId: Long,
    onClose: () -> Unit,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    // mediaId is read by the ViewModel via SavedStateHandle. We keep the
    // parameter on the public signature for explicit, type-safe nav-call sites.
    val state by viewModel.state.collectAsStateWithLifecycle()
    PreviewScreen(
        state = state,
        onClose = onClose,
        onBin = {
            viewModel.bin()
            onClose()
        },
        onKeep = {
            viewModel.keep()
            onClose()
        },
        onFavorite = {
            viewModel.favorite()
            onClose()
        }
    )
}

@Composable
private fun PreviewScreen(
    state: PreviewUiState,
    onClose: () -> Unit,
    onBin: () -> Unit,
    onKeep: () -> Unit,
    onFavorite: () -> Unit
) {
    val item = state.item
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissOffset = remember { Animatable(0f) }
    var metadataVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(dismissOffset.value, density) {
        val dismissThresholdPx = with(density) { 220.dp.toPx() }
        if (abs(dismissOffset.value) > dismissThresholdPx) {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(item?.id) {
                if (item == null) return@pointerInput
                detectVerticalDragGestures(
                    onVerticalDrag = { change, drag ->
                        change.consume()
                        scope.launch {
                            dismissOffset.snapTo(dismissOffset.value + drag)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            dismissOffset.animateTo(0f, animationSpec = tween(200))
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            dismissOffset.animateTo(0f, animationSpec = tween(200))
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = dismissOffset.value }
        ) {
            when {
                item == null && state.loading -> { /* loading shimmer slot */ }
                item == null -> {
                    Text(
                        text = "Media unavailable",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                item.isVideo -> {
                    VideoPlayer(
                        uri = item.uri,
                        autoplay = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    ZoomableImage(
                        uri = item.uri,
                        contentDescription = item.displayName,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        TopBar(
            onClose = onClose,
            onToggleMetadata = { metadataVisible = !metadataVisible },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        ActionRail(
            onBin = onBin,
            onKeep = onKeep,
            onFavorite = onFavorite,
            onShare = {
                item?.let { i ->
                    shareMedia(context, i)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        AnimatedVisibility(
            visible = metadataVisible && item != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            item?.let { MetadataDrawer(item = it) }
        }
    }
}

@Composable
private fun TopBar(
    onClose: () -> Unit,
    onToggleMetadata: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.preview_dismiss),
                tint = Color.White
            )
        }
        IconButton(onClick = onToggleMetadata) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = stringResource(R.string.preview_metadata),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ActionRail(
    onBin: () -> Unit,
    onKeep: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Black.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBin) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.preview_bin),
                    tint = Color.White
                )
            }
            IconButton(onClick = onKeep) {
                Icon(
                    Icons.Rounded.Favorite,
                    contentDescription = stringResource(R.string.preview_keep),
                    tint = Color.White
                )
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(R.string.preview_favorite),
                    tint = Color.White
                )
            }
            IconButton(onClick = onShare) {
                Icon(
                    Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.preview_share),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun MetadataDrawer(item: MediaItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        color = Color.Black.copy(alpha = 0.82f),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(PaddingValues(horizontal = 20.dp, vertical = 16.dp)),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(item.displayName, style = MaterialTheme.typography.titleMedium)
            Text(
                "${ByteFormat.format(item.sizeBytes)} · ${item.resolutionLabel}",
                style = MaterialTheme.typography.bodyMedium
            )
            val dateLabel = remember(item.dateTakenMs) {
                SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault())
                    .format(Date(item.dateTakenMs))
            }
            Text(dateLabel, style = MaterialTheme.typography.bodySmall)
            Text(item.folderName, style = MaterialTheme.typography.bodySmall)
            item.durationMs?.let {
                Text(
                    "Duration ${it / 1000}s",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun shareMedia(context: android.content.Context, item: MediaItem) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = item.mimeType
        putExtra(Intent.EXTRA_STREAM, item.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
