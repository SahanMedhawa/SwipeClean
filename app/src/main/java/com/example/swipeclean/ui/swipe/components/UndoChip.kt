package com.example.swipeclean.ui.swipe.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun UndoChip(
    visible: Boolean,
    token: Int,
    onUndo: () -> Unit,
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(token, visible) {
        if (visible) {
            delay(UNDO_TIMEOUT_MS)
            onTimeout()
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .clickable(onClick = onUndo)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Undo,
                contentDescription = null,
                tint = Color(0xFF1a1c1e),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Undo last action",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1a1c1e)
            )
        }
    }
}

private const val UNDO_TIMEOUT_MS = 4_000L
