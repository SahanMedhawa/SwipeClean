package com.example.swipeclean.ui.swipe.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.swipeclean.R
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
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            tonalElevation = 8.dp,
            modifier = Modifier.clickable(onClick = onUndo)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.swipe_undo_chip),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

private const val UNDO_TIMEOUT_MS = 4_000L
