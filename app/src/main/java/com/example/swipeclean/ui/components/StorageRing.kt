package com.example.swipeclean.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.swipeclean.ui.theme.ActionBin

/**
 * Animated three-segment ring: used / reclaimable / free.
 *
 * The "used" segment is the device's total used storage; "reclaimable" is
 * carved out of "used" to highlight bin contents. The remaining wedge is "free".
 */
@Composable
fun StorageRing(
    totalBytes: Long,
    usedBytes: Long,
    reclaimableBytes: Long,
    modifier: Modifier = Modifier,
    centerLabel: @Composable () -> Unit
) {
    val total = totalBytes.coerceAtLeast(1L).toFloat()
    val used = usedBytes.coerceIn(0L, totalBytes).toFloat()
    val reclaimable = reclaimableBytes.coerceIn(0L, usedBytes).toFloat()

    val targetUsedFraction = used / total
    val targetReclaimableFraction = reclaimable / total

    val animatedUsed = remember { Animatable(0f) }
    val animatedReclaimable = remember { Animatable(0f) }

    LaunchedEffect(targetUsedFraction, targetReclaimableFraction) {
        animatedUsed.animateTo(
            targetValue = targetUsedFraction,
            animationSpec = tween(durationMillis = 900, easing = LinearOutSlowInEasing)
        )
    }
    LaunchedEffect(targetReclaimableFraction) {
        animatedReclaimable.animateTo(
            targetValue = targetReclaimableFraction,
            animationSpec = tween(durationMillis = 900, delayMillis = 200, easing = LinearOutSlowInEasing)
        )
    }

    val track = MaterialTheme.colorScheme.surfaceVariant
    val usedColor = MaterialTheme.colorScheme.primary
    val reclaimableColor = ActionBin

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val side = if (maxWidth < maxHeight) maxWidth else maxHeight
        Canvas(modifier = Modifier.size(side)) {
            val strokePx = size.minDimension * 0.12f
            val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)
            val inset = strokePx / 2f
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - strokePx,
                height = size.height - strokePx
            )

            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )

            val usedSweep = 360f * animatedUsed.value
            val reclaimableSweep = 360f * animatedReclaimable.value
            val baseUsedSweep = (usedSweep - reclaimableSweep).coerceAtLeast(0f)

            if (baseUsedSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(usedColor, usedColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = baseUsedSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
            }
            if (reclaimableSweep > 0f) {
                drawArc(
                    color = reclaimableColor,
                    startAngle = -90f + baseUsedSweep,
                    sweepAngle = reclaimableSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
            centerLabel()
        }
    }
}

@Composable
fun StorageLegend(
    usedLabel: String,
    reclaimableLabel: String,
    freeLabel: String,
    modifier: Modifier = Modifier
) {
    val usedColor = MaterialTheme.colorScheme.primary
    val freeColor = MaterialTheme.colorScheme.surfaceVariant
    val reclaimableColor = ActionBin

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendDot(usedColor, "Used", usedLabel)
        LegendDot(reclaimableColor, "Reclaimable", reclaimableLabel)
        LegendDot(freeColor, "Free", freeLabel)
    }
}

@Composable
private fun LegendDot(color: Color, title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .padding(2.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(color) }
            }
            Box(Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
