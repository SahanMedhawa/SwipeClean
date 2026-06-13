package com.example.swipeclean.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.swipeclean.domain.model.BinnedMedia
import com.example.swipeclean.domain.repository.BinRepository
import com.example.swipeclean.domain.repository.SettingsRepository
import com.example.swipeclean.notification.BinExpiryNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic worker that scans the bin for items within [WARNING_WINDOW_DAYS] of
 * their auto-purge date and posts a single roll-up notification if any match.
 *
 * Respects the user's [com.example.swipeclean.domain.model.AppSettings.binNotificationsEnabled]
 * preference.
 */
@HiltWorker
class BinExpiryReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val binRepository: BinRepository,
    private val settingsRepository: SettingsRepository,
    private val notifier: BinExpiryNotifier
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settings = settingsRepository.current()
            if (!settings.binNotificationsEnabled) return Result.success()

            val now = System.currentTimeMillis()
            val ttl = settings.binAutoDeleteDays * BinnedMedia.MILLIS_PER_DAY
            val warningWindow = WARNING_WINDOW_DAYS * BinnedMedia.MILLIS_PER_DAY

            val all = binRepository.getAll()
            val expiringSoon = all.filter { item ->
                val expiresAt = item.binnedAt + ttl
                val remaining = expiresAt - now
                remaining in 0..warningWindow
            }

            if (expiringSoon.isNotEmpty()) {
                val daysRemaining = WARNING_WINDOW_DAYS.toInt()
                notifier.notifyExpiringSoon(expiringSoon.size, daysRemaining)
            }
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "swipeclean_bin_expiry_reminder"
        const val WARNING_WINDOW_DAYS: Long = 5
    }
}
