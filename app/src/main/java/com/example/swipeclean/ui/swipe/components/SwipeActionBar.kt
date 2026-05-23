package com.example.swipeclean.ui.swipe.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.swipeclean.R
import com.example.swipeclean.haptics.rememberTapHaptic
import com.example.swipeclean.ui.theme.ActionBin
import com.example.swipeclean.ui.theme.ActionFave
import com.example.swipeclean.ui.theme.ActionKeep

@Composable
fun SwipeActionBar(
    onUndo: () -> Unit,
    onBin: () -> Unit,
    onKeep: () -> Unit,
    onFavorite: () -> Unit,
    canUndo: Boolean,
    modifier: Modifier = Modifier
) {
    val tap = rememberTapHaptic()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            icon = Icons.Rounded.Replay,
            contentDescription = stringResource(R.string.swipe_action_undo),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            background = MaterialTheme.colorScheme.surfaceVariant,
            size = 52.dp,
            enabled = canUndo,
            onClick = {
                tap()
                onUndo()
            }
        )
        ActionButton(
            icon = Icons.Rounded.Delete,
            contentDescription = stringResource(R.string.swipe_action_bin),
            tint = Color.White,
            background = ActionBin,
            size = 64.dp,
            onClick = {
                tap()
                onBin()
            }
        )
        ActionButton(
            icon = Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(R.string.swipe_action_favorite),
            tint = Color.White,
            background = ActionFave,
            size = 56.dp,
            onClick = {
                tap()
                onFavorite()
            }
        )
        ActionButton(
            icon = Icons.Rounded.Favorite,
            contentDescription = stringResource(R.string.swipe_action_keep),
            tint = Color.White,
            background = ActionKeep,
            size = 64.dp,
            onClick = {
                tap()
                onKeep()
            }
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    background: Color,
    size: androidx.compose.ui.unit.Dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (enabled) background else background.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(size),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = tint,
                disabledContentColor = tint.copy(alpha = 0.5f)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}
