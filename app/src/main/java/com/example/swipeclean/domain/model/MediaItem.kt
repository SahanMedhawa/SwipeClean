package com.example.swipeclean.domain.model

import android.net.Uri

/**
 * Pure-domain representation of a single photo or video on the device.
 * Built from a MediaStore cursor row. Holds no Android framework references
 * other than [Uri].
 */
data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val durationMs: Long?,
    val dateTakenMs: Long,
    val folderName: String,
    val isVideo: Boolean
) {
    val resolutionLabel: String get() = if (width > 0 && height > 0) "${width}×${height}" else "—"
}
