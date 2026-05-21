package com.example.swipeclean.domain.usecase

import android.content.IntentSender
import com.example.swipeclean.domain.repository.BinRepository
import javax.inject.Inject

class EmptyBin @Inject constructor(
    private val binRepository: BinRepository
) {
    /**
     * Returns the [IntentSender] that, once confirmed, physically deletes
     * every item currently in the bin. Pair with [BinRepository.confirmPermanentlyDeleted].
     */
    suspend fun buildIntent(): EmptyBinRequest? {
        val items = binRepository.getAll()
        if (items.isEmpty()) return null
        val ids = items.map { it.mediaId }
        val sender = binRepository.buildDeleteRequest(ids) ?: return null
        return EmptyBinRequest(
            intentSender = sender,
            mediaIds = ids,
            totalBytes = items.sumOf { it.sizeBytes }
        )
    }

    data class EmptyBinRequest(
        val intentSender: IntentSender,
        val mediaIds: List<Long>,
        val totalBytes: Long
    )
}
