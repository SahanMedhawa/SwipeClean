package com.example.swipeclean.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row analytics aggregate. Always lives at [SINGLETON_ID] = 1.
 *
 * Adding new counters is non-breaking — we keep nullable defaults so an existing
 * row can be re-read without a migration.
 */
@Entity(tableName = "analytics_counter")
data class AnalyticsCounterEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val totalReviewed: Long = 0,
    val totalBinned: Long = 0,
    val totalKept: Long = 0,
    val totalFavorited: Long = 0,
    val totalPermanentlyDeleted: Long = 0,
    val totalReclaimedBytes: Long = 0,
    val fastestSessionMs: Long? = null,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val lastSessionDay: Long = 0
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
