package com.example.swipeclean.ui.bin.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.swipeclean.R
import com.example.swipeclean.util.ByteFormat

@Composable
fun EmptyBinDialog(
    totalBytes: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sizeLabel = ByteFormat.format(totalBytes)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bin_confirm_title)) },
        text = { Text(stringResource(R.string.bin_confirm_message, sizeLabel)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.bin_confirm_cta, sizeLabel))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.bin_cancel))
            }
        }
    )
}
