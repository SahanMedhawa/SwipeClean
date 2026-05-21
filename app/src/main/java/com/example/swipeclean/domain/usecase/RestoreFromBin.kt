package com.example.swipeclean.domain.usecase

import com.example.swipeclean.domain.repository.BinRepository
import javax.inject.Inject

class RestoreFromBin @Inject constructor(
    private val binRepository: BinRepository
) {
    suspend operator fun invoke(mediaId: Long) = binRepository.restore(mediaId)
    suspend operator fun invoke(mediaIds: List<Long>) {
        mediaIds.forEach { binRepository.restore(it) }
    }
}
