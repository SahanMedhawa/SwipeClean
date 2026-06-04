package com.example.swipeclean.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swipeclean.R
import com.example.swipeclean.haptics.rememberTapHaptic
import com.example.swipeclean.ui.components.StatCard
import com.example.swipeclean.ui.components.StorageLegend
import com.example.swipeclean.ui.components.StorageRing
import com.example.swipeclean.ui.theme.ActionBin
import com.example.swipeclean.ui.theme.ActionFave
import com.example.swipeclean.ui.theme.ActionKeep
import com.example.swipeclean.util.ByteFormat

@Composable
fun HomeRoute(
    onStartCleaning: () -> Unit,
    onOpenBin: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onStartCleaning = onStartCleaning,
        onOpenBin = onOpenBin,
        onOpenAnalytics = onOpenAnalytics,
        onOpenSettings = onOpenSettings
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onStartCleaning: () -> Unit,
    onOpenBin: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val tap = rememberTapHaptic()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            HeaderRow(
                state = state,
                onOpenAnalytics = onOpenAnalytics,
                onOpenSettings = onOpenSettings,
                onTapFeedback = tap
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 12.dp)
            ) {
                StorageRing(
                    totalBytes = state.storage.totalBytes,
                    usedBytes = state.storage.usedBytes,
                    reclaimableBytes = state.reclaimableBytes,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = ByteFormat.format(state.storage.freeBytes),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "free of ${ByteFormat.format(state.storage.totalBytes)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            StorageLegend(
                usedLabel = ByteFormat.format(state.storage.usedBytes),
                reclaimableLabel = ByteFormat.format(state.reclaimableBytes),
                freeLabel = ByteFormat.format(state.storage.freeBytes)
            )

            StatGrid(state = state)

            StreakChip(streakDays = state.streakDays)

            PrimaryCleaningCta(onStartCleaning = onStartCleaning)
            SecondaryBinCta(binCount = state.binCount, onOpenBin = onOpenBin)

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun HeaderRow(
    state: HomeUiState,
    onOpenAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    onTapFeedback: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row {
            IconButton(onClick = {
                onTapFeedback()
                onOpenAnalytics()
            }) {
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = stringResource(R.string.analytics_title)
                )
            }
            IconButton(onClick = {
                onTapFeedback()
                onOpenSettings()
            }) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.settings_title)
                )
            }
        }
    }
}

@Composable
private fun StatGrid(state: HomeUiState) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = stringResource(R.string.home_stat_photos),
                value = state.photoCount.toString(),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.primary
            )
            StatCard(
                label = stringResource(R.string.home_stat_videos),
                value = state.videoCount.toString(),
                modifier = Modifier.weight(1f),
                accent = ActionFave
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = stringResource(R.string.home_stat_in_bin),
                value = state.binCount.toString(),
                modifier = Modifier.weight(1f),
                accent = ActionBin
            )
            StatCard(
                label = stringResource(R.string.home_stat_reclaimable),
                value = ByteFormat.format(state.reclaimableBytes),
                modifier = Modifier.weight(1f),
                accent = ActionKeep
            )
        }
        StatCard(
            label = stringResource(R.string.home_stat_duplicates),
            value = "—",
            modifier = Modifier.fillMaxWidth(),
            accent = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun StreakChip(streakDays: Int) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (streakDays > 0) {
                    stringResource(R.string.home_streak, streakDays)
                } else {
                    stringResource(R.string.home_streak_zero)
                },
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun PrimaryCleaningCta(onStartCleaning: () -> Unit) {
    val tap = rememberTapHaptic()
    Button(
        onClick = {
            tap()
            onStartCleaning()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(horizontal = 24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = stringResource(R.string.home_start_cleaning),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun SecondaryBinCta(binCount: Int, onOpenBin: () -> Unit) {
    val tap = rememberTapHaptic()
    FilledTonalButton(
        onClick = {
            tap()
            onOpenBin()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = if (binCount > 0) {
                "${stringResource(R.string.home_view_bin)} ($binCount)"
            } else {
                stringResource(R.string.home_view_bin)
            },
            style = MaterialTheme.typography.titleMedium
        )
    }
}
