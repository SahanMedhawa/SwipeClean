package com.example.swipeclean.ui.bin

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swipeclean.R
import com.example.swipeclean.ui.bin.components.BinItemCard
import com.example.swipeclean.ui.bin.components.EmptyBinDialog
import com.example.swipeclean.ui.theme.ActionBin

import com.example.swipeclean.util.ByteFormat

@Composable
fun BinRoute(
    onBack: () -> Unit,
    viewModel: BinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BinScreen(
        state = state,
        onBack = onBack,
        onRestore = viewModel::restore,
        onDelete = viewModel::deleteOne,
        onEmptyRequested = viewModel::openConfirmEmpty,
        onConfirmEmpty = viewModel::requestEmpty,
        onDismissConfirm = viewModel::dismissConfirm
    )
}

@Composable
private fun BinScreen(
    state: BinUiState,
    onBack: () -> Unit,
    onRestore: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onEmptyRequested: () -> Unit,
    onConfirmEmpty: () -> Unit,
    onDismissConfirm: () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val onBgColor = MaterialTheme.colorScheme.onBackground

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            TopBar(
                onBack = onBack,
                onEmptyRequested = onEmptyRequested,
                itemCount = state.items.size,
                onBgColor = onBgColor
            )

            if (state.items.isEmpty()) {
                EmptyState(modifier = Modifier.weight(1f))
            } else {
                SummaryCard(
                    itemCount = state.items.size,
                    totalSizeBytes = state.totalSizeBytes,
                    autoDeleteDays = state.autoDeleteDays
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f).fillMaxSize()
                ) {
                    items(state.items, key = { it.mediaId }) { item ->
                        BinItemCard(
                            item = item,
                            autoDeleteDays = state.autoDeleteDays,
                            now = state.now,
                            onRestore = { onRestore(item.mediaId) },
                            onDelete = { onDelete(item.mediaId) },

                        )
                    }
                }
            }
        }
    }

    if (state.showConfirm) {
        EmptyBinDialog(
            totalBytes = state.totalSizeBytes,
            onConfirm = onConfirmEmpty,
            onDismiss = onDismissConfirm
        )
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    onEmptyRequested: () -> Unit,
    itemCount: Int,
    onBgColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = onBgColor)
            }
            Text(
                text = "Recycle bin",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = onBgColor
                ),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        if (itemCount > 0) {
            TextButton(
                onClick = onEmptyRequested,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = null,
                    tint = ActionBin,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    "Empty bin",
                    color = ActionBin,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    itemCount: Int,
    totalSizeBytes: Long,
    autoDeleteDays: Int
) {
    val cardBg = MaterialTheme.colorScheme.surface
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardBg
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$itemCount items · ${ByteFormat.format(totalSizeBytes)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Items will be permanently deleted after $autoDeleteDays days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onBackground
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.bin_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.bin_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
