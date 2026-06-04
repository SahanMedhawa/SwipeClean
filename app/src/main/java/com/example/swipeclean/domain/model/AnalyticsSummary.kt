package com.example.swipeclean.domain.model

data class AnalyticsSummary(
    val totalReviewed: Long = 0,
    val totalBinned: Long = 0,
    val totalKept: Long = 0,
    val totalFavorited: Long = 0,
    val totalPermanentlyDeleted: Long = 0,
    val totalReclaimedBytes: Long = 0,
    val fastestSessionMs: Long? = null,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val topFolder: TopFolder? = null
) {
    val keepRatio: Float
        get() {
            val denom = (totalBinned + totalKept + totalFavorited).toFloat()
            return if (denom <= 0f) 0.5f else (totalKept + totalFavorited).toFloat() / denom
        }

    data class TopFolder(val name: String, val count: Long)
}
