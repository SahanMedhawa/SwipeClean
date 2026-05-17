package com.example.swipeclean.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per swipe action recorded during a session. Used to back the undo stack.
 *
 * Rows for a session are deleted when [sessionId] no longer matches the active session.
 */
@Entity(tableName = "swipe_actions")
data class SwipeActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val mediaId: Long,
    val action: String,
    val timestamp: Long
)
