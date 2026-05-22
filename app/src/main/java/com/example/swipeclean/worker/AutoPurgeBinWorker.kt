package com.example.swipeclean.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.swipeclean.domain.usecase.AutoPurgeExpiredBin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Daily worker that finds bin entries past the user-configured retention window.
 *
 * Note: we don't physically delete from MediaStore here. Scoped storage on
 * Android 11+ requires user-confirmed IntentSender flows that can only be
 * launched from an Activity. Instead, the worker collects expired ids and
 * — in Phase 3 — surfaces a notification deep-linking back to the bin screen
 * where the user can tap "Empty bin" to fire the consent dialog.
 *
 * For Phase 1 the worker just runs the query (proving the schedule is wired)
 * and returns SUCCESS. Phase 3 will plug in the notification.
 */
@HiltWorker
class AutoPurgeBinWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val autoPurgeExpiredBin: AutoPurgeExpiredBin
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val expired = autoPurgeExpiredBin()
            // TODO Phase 3: if (expired.isNotEmpty()) BinExpiryNotifier.notifyPending(expired)
            // Output the count so it's observable in WorkManager logs.
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "swipeclean_auto_purge_bin"
    }
}
