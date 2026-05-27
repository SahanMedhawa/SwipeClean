package com.example.swipeclean.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.swipeclean.ui.theme.ActionBin
import com.example.swipeclean.ui.theme.ActionFave
import com.example.swipeclean.ui.theme.ActionKeep
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Compose-only illustrations for the four onboarding pages.
 *
 * No external assets needed — we draw card shapes, rings and arrows on a
 * Canvas so the visuals respect dynamic color and reduced-motion friendly.
 */

@Composable
fun SwipeIllustration(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "swipe-loop")
    val offset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "swipe-x"
    )
    val tint = ActionBin
    val keepTint = ActionKeep
    Canvas(modifier = modifier.fillMaxSize()) {
        val cardW = size.width * 0.55f
        val cardH = size.height * 0.58f
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Background card (static)
        drawRoundedCardOutline(cx - cardW / 2f, cy - cardH / 2f + 14f, cardW, cardH, tint.copy(alpha = 0.18f))
        drawRoundedCardOutline(cx - cardW / 2f, cy - cardH / 2f + 8f, cardW, cardH, keepTint.copy(alpha = 0.22f))

        // Active swiping card
        val phase = ((offset * 2f) - 1f).coerceIn(-1f, 1f)
        val translation = phase * size.width * 0.35f
        val rotation = phase * 18f
        translate(translation, 0f) {
            rotate(rotation, pivot = Offset(cx, cy)) {
                drawRoundedCardFilled(cx - cardW / 2f, cy - cardH / 2f, cardW, cardH, keepTint)
            }
        }
    }
}

@Composable
fun BinIllustration(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "bin-loop")
    val drop by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bin-drop"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val binW = size.width * 0.45f
        val binH = size.height * 0.42f
        val cx = size.width / 2f
        val cy = size.height * 0.62f

        val cardSide = size.width * 0.22f
        val travel = size.height * 0.36f
        val cardY = cy - binH / 2f - cardSide + (travel * drop)
        val cardAlpha = (1f - drop).coerceIn(0f, 1f)
        drawRoundedCardFilled(
            cx - cardSide / 2f,
            cardY,
            cardSide,
            cardSide,
            ActionBin.copy(alpha = 0.85f * cardAlpha)
        )

        // Bin body
        drawRoundedCardOutline(cx - binW / 2f, cy - binH / 2f, binW, binH, ActionBin, stroke = 8f)
        // Lid
        drawRoundedCardFilled(cx - binW / 2f - 12f, cy - binH / 2f - 18f, binW + 24f, 12f, ActionBin)
    }
}

@Composable
fun ProgressIllustration(modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = 0.78f,
        animationSpec = tween(1_200),
        label = "progress"
    )
    val color = ActionKeep
    Canvas(modifier = modifier.fillMaxSize()) {
        val side = size.minDimension * 0.7f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val stroke = side * 0.10f
        val arcSize = Size(side, side)
        val topLeft = Offset(cx - side / 2f, cy - side / 2f)
        // Track
        drawArc(
            color = color.copy(alpha = 0.15f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        // Progress
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = stroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
fun StarIllustration(modifier: Modifier = Modifier) {
    var pulse by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            pulse = 0f
            delay(80)
            pulse = 1f
            delay(700)
        }
    }
    val animated by animateFloatAsState(
        targetValue = pulse,
        animationSpec = tween(700),
        label = "star-pulse"
    )
    val density = LocalDensity.current
    val strokePx = with(density) { 6.dp.toPx() }
    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outer = size.minDimension * (0.30f + 0.04f * animated)
        val inner = outer * 0.45f
        val color = ActionFave
        val path = androidx.compose.ui.graphics.Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outer else inner
            val theta = Math.toRadians((-90 + i * 36).toDouble())
            val px = (cx + r * cos(theta)).toFloat()
            val py = (cy + r * sin(theta)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path, color = color.copy(alpha = 0.25f))
        drawPath(
            path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokePx,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundedCardFilled(
    x: Float, y: Float, w: Float, h: Float, color: Color
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f, 28f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundedCardOutline(
    x: Float, y: Float, w: Float, h: Float, color: Color, stroke: Float = 4f
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f, 28f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
    )
}

