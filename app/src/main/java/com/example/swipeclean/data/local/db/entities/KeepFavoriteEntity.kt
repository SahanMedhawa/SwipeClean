package com.example.swipeclean.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted "kept" or "favorited" media. We use the same table for both
 * states (distinguished by [isFavorite]) so the review queue can exclude
 * them with a single join.
 */
@Entity(tableName = "keep_favorite")
data class KeepFavoriteEntity(
    @PrimaryKey val mediaId: Long,
    val isFavorite: Boolean,
    val recordedAt: Long
)
