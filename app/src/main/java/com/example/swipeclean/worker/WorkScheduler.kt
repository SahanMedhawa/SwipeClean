package com.example.swipeclean.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleAutoPurge() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val purge = PeriodicWorkRequestBuilder<AutoPurgeBinWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        val reminder = PeriodicWorkRequestBuilder<BinExpiryReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).apply {
            enqueueUniquePeriodicWork(
                AutoPurgeBinWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                purge
            )
            enqueueUniquePeriodicWork(
                BinExpiryReminderWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                reminder
            )
        }
    }
}
