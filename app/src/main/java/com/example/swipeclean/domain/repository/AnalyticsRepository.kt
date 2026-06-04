package com.example.swipeclean.domain.repository

import com.example.swipeclean.domain.model.AnalyticsSummary
import com.example.swipeclean.domain.model.SwipeAction
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {

    fun observeSummary(): Flow<AnalyticsSummary>

    suspend fun recordSwipe(action: SwipeAction, folderName: String, sizeBytes: Long)

    suspend fun recordPermanentDeletion(reclaimedBytes: Long)

    suspend fun recordSessionDuration(durationMs: Long)

    suspend fun reset()
}
