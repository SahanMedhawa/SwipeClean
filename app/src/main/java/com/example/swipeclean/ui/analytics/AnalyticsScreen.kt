package com.example.swipeclean.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.swipeclean.R
import com.example.swipeclean.domain.model.AnalyticsSummary
import com.example.swipeclean.ui.theme.AccentCyan
import com.example.swipeclean.ui.theme.ActionBin
import com.example.swipeclean.ui.theme.ActionKeep

import com.example.swipeclean.ui.theme.PrimaryFixed
import com.example.swipeclean.ui.theme.OnPrimaryFixed
import com.example.swipeclean.ui.theme.SecondaryFixed
import com.example.swipeclean.ui.theme.OnSecondaryFixed
import com.example.swipeclean.ui.theme.TertiaryFixed
import com.example.swipeclean.ui.theme.OnTertiaryFixed
import com.example.swipeclean.util.ByteFormat

@Composable
fun AnalyticsRoute(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AnalyticsScreen(state = state, onBack = onBack)
}

@Composable
private fun AnalyticsScreen(state: AnalyticsUiState, onBack: () -> Unit) {
    val bgColor = MaterialTheme.colorScheme.background
    val onBgColor = MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack, 
                            contentDescription = null,
                            tint = onBgColor
                        )
                    }
                    Text(
                        text = "Your clean-up",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = onBgColor
                        )
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Settings, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val summary = state.summary
            val empty = summary.totalReviewed == 0L
            if (empty) {
                Text(
                    text = stringResource(R.string.analytics_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                )
            } else {
                MetricsGrid(summary = summary)
                KeepBinRatio(summary = summary)
                ActivityRows(summary = summary)
                HeroFooter()
            }
        }
    }
}

@Composable
private fun MetricsGrid(summary: AnalyticsSummary) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val reviewedBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else PrimaryFixed
    val reviewedOnBg = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else OnPrimaryFixed
    val binnedBg = if (isDark) MaterialTheme.colorScheme.secondaryContainer else SecondaryFixed
    val binnedOnBg = if (isDark) MaterialTheme.colorScheme.onSecondaryContainer else OnSecondaryFixed
    val deletedBg = MaterialTheme.colorScheme.surfaceVariant
    val deletedOnBg = MaterialTheme.colorScheme.onSurfaceVariant
    val reclaimedBg = if (isDark) MaterialTheme.colorScheme.errorContainer else TertiaryFixed
    val reclaimedOnBg = if (isDark) MaterialTheme.colorScheme.onErrorContainer else OnTertiaryFixed

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GridCard(
                label = "REVIEWED",
                value = summary.totalReviewed.toString(),
                containerColor = reviewedBg,
                contentColor = reviewedOnBg,
                modifier = Modifier.weight(1f)
            )
            GridCard(
                label = "SENT TO BIN",
                value = summary.totalBinned.toString(),
                containerColor = binnedBg,
                contentColor = binnedOnBg,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GridCard(
                label = "PERMANENTLY\nDELETED",
                value = summary.totalPermanentlyDeleted.toString(),
                containerColor = deletedBg,
                contentColor = deletedOnBg,
                modifier = Modifier.weight(1f)
            )
            GridCard(
                label = "STORAGE\nRECLAIMED",
                value = ByteFormat.format(summary.totalReclaimedBytes).replace(" ", ""),
                containerColor = reclaimedBg,
                contentColor = reclaimedOnBg,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GridCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.aspectRatio(0.9f),
        shape = RoundedCornerShape(24.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor.copy(alpha = 0.8f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 36.sp
                ),
                color = contentColor
            )
        }
    }
}

@Composable
private fun KeepBinRatio(summary: AnalyticsSummary) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface
    val keepColor = MaterialTheme.colorScheme.secondary
    val binColor = MaterialTheme.colorScheme.error

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = surfaceColor,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Keep vs bin",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                val keepRatio = summary.keepRatio.coerceIn(0f, 1f)
                val keepPercent = (keepRatio * 100).toInt()
                val binPercent = ((1f - keepRatio) * 100).toInt()

                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(if (keepRatio > 0f) keepRatio else 0.001f)
                            .fillMaxSize()
                            .background(keepColor),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (keepRatio > 0.1f) {
                            Text(
                                "$keepPercent%",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(if (keepRatio < 1f) 1f - keepRatio else 0.001f)
                            .fillMaxSize()
                            .background(binColor),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (keepRatio < 0.9f) {
                            Text(
                                "$binPercent%",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(keepColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kept ${(summary.keepRatio * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = textColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Binned ${((1f - summary.keepRatio) * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = textColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).background(binColor, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun ActivityRows(summary: AnalyticsSummary) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val iconBgColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(88.dp),
            shape = RoundedCornerShape(24.dp),
            color = surfaceColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("STREAK", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = textColor.copy(alpha = 0.6f))
                    Text("${summary.currentStreakDays} days", style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Medium), color = textColor)
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(88.dp),
            shape = RoundedCornerShape(24.dp),
            color = surfaceColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("MOST CLEANED", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = textColor.copy(alpha = 0.6f))
                    Text(summary.topFolder?.name ?: "—", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = textColor)
                }
            }
        }
    }
}

@Composable
private fun HeroFooter() {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val containerColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFF004b71)
    val contentColor = Color.White

    Surface(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(48.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Every swipe\ncounts.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = contentColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You're making space for new\nmemories.",
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
