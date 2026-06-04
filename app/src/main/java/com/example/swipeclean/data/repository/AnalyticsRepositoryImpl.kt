package com.example.swipeclean.data.repository

import com.example.swipeclean.data.local.db.dao.AnalyticsDao
import com.example.swipeclean.data.local.db.entities.AnalyticsCounterEntity
import com.example.swipeclean.data.local.db.entities.FolderCountEntity
import com.example.swipeclean.domain.model.AnalyticsSummary
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.domain.repository.AnalyticsRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao
) : AnalyticsRepository {

    override fun observeSummary(): Flow<AnalyticsSummary> = combine(
        analyticsDao.observeCounters().map { it ?: AnalyticsCounterEntity() },
        analyticsDao.observeTopFolders(1)
    ) { counters, folders ->
        AnalyticsSummary(
            totalReviewed = counters.totalReviewed,
            totalBinned = counters.totalBinned,
            totalKept = counters.totalKept,
            totalFavorited = counters.totalFavorited,
            totalPermanentlyDeleted = counters.totalPermanentlyDeleted,
            totalReclaimedBytes = counters.totalReclaimedBytes,
            fastestSessionMs = counters.fastestSessionMs,
            currentStreakDays = counters.currentStreakDays,
            longestStreakDays = counters.longestStreakDays,
            topFolder = folders.firstOrNull()?.let {
                AnalyticsSummary.TopFolder(it.folderName, it.cleanedCount)
            }
        )
    }

    override suspend fun recordSwipe(action: SwipeAction, folderName: String, sizeBytes: Long) {
        val current = analyticsDao.getCounters() ?: AnalyticsCounterEntity()
        val today = todayDayOfEpoch()
        val (currentStreak, longestStreak) = updateStreak(current, today)
        val updated = current.copy(
            totalReviewed = current.totalReviewed + 1,
            totalBinned = current.totalBinned + if (action == SwipeAction.BIN) 1 else 0,
            totalKept = current.totalKept + if (action == SwipeAction.KEEP) 1 else 0,
            totalFavorited = current.totalFavorited + if (action == SwipeAction.FAVORITE) 1 else 0,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            lastSessionDay = today
        )
        analyticsDao.upsertCounters(updated)

        // Folder tally
        val folder = analyticsDao.getFolder(folderName)
        analyticsDao.upsertFolder(
            FolderCountEntity(folderName = folderName, cleanedCount = (folder?.cleanedCount ?: 0L) + 1)
        )

        // sizeBytes is reserved for future "biggest-file" stats; keep the param wired.
        @Suppress("UNUSED_VARIABLE")
        val _bytes = sizeBytes
    }

    override suspend fun recordPermanentDeletion(reclaimedBytes: Long) {
        val current = analyticsDao.getCounters() ?: AnalyticsCounterEntity()
        analyticsDao.upsertCounters(
            current.copy(
                totalPermanentlyDeleted = current.totalPermanentlyDeleted + 1,
                totalReclaimedBytes = current.totalReclaimedBytes + reclaimedBytes
            )
        )
    }

    override suspend fun recordSessionDuration(durationMs: Long) {
        val current = analyticsDao.getCounters() ?: AnalyticsCounterEntity()
        val fastest = current.fastestSessionMs
        val newFastest = if (fastest == null || durationMs < fastest) durationMs else fastest
        analyticsDao.upsertCounters(current.copy(fastestSessionMs = newFastest))
    }

    override suspend fun reset() {
        analyticsDao.clearCounters()
        analyticsDao.clearFolders()
    }

    private fun updateStreak(current: AnalyticsCounterEntity, today: Long): Pair<Int, Int> {
        val last = current.lastSessionDay
        val newStreak = when {
            last == 0L -> 1
            today == last -> current.currentStreakDays.coerceAtLeast(1)
            today == last + 1 -> current.currentStreakDays + 1
            else -> 1
        }
        val longest = newStreak.coerceAtLeast(current.longestStreakDays)
        return newStreak to longest
    }

    private fun todayDayOfEpoch(): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis / (24L * 60 * 60 * 1000)
    }
}
