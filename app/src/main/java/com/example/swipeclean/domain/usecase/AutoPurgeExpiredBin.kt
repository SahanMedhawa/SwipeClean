package com.example.swipeclean.domain.usecase

import com.example.swipeclean.data.local.db.dao.BinDao
import com.example.swipeclean.domain.model.BinnedMedia
import com.example.swipeclean.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Returns the bin rows whose age exceeds the user-configured retention window.
 * Used by [com.example.swipeclean.worker.AutoPurgeBinWorker] to decide what to
 * delete. The actual deletion still requires user-confirmed IntentSender flow,
 * so the worker posts a notification rather than deleting silently.
 */
class AutoPurgeExpiredBin @Inject constructor(
    private val binDao: BinDao,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()): List<Long> {
        val days = settingsRepository.current().binAutoDeleteDays
        val threshold = now - days * BinnedMedia.MILLIS_PER_DAY
        return binDao.getExpired(threshold).map { it.mediaId }
    }
}
