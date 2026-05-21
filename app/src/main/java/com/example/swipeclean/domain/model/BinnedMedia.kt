package com.example.swipeclean.domain.model

import android.net.Uri

/**
 * Domain mirror of [com.example.swipeclean.data.local.db.entities.BinnedMediaEntity].
 * The Room entity is an implementation detail of the data layer; UI + use cases
 * should consume this type.
 */
data class BinnedMedia(
    val mediaId: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val durationMs: Long?,
    val dateTakenMs: Long,
    val folderName: String,
    val binnedAt: Long,
    val isVideo: Boolean
) {
    fun daysRemainingUntil(autoDeleteDays: Int, now: Long): Int {
        val expiresAt = binnedAt + autoDeleteDays * MILLIS_PER_DAY
        val remaining = expiresAt - now
        if (remaining <= 0) return 0
        return ((remaining + MILLIS_PER_DAY - 1) / MILLIS_PER_DAY).toInt()
    }

    companion object {
        const val MILLIS_PER_DAY: Long = 24L * 60 * 60 * 1000
    }
}
