package com.example.swipeclean.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder_counts")
data class FolderCountEntity(
    @PrimaryKey val folderName: String,
    val cleanedCount: Long
)
