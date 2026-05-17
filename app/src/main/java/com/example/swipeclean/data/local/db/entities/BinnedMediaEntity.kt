package com.example.swipeclean.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "binned_media")
data class BinnedMediaEntity(
    @PrimaryKey val mediaId: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val duration: Long?,
    val dateTaken: Long,
    val folderName: String,
    val binnedAt: Long,
    val thumbnailPath: String?,
    val isVideo: Boolean
)
