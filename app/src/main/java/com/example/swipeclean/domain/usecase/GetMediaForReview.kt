package com.example.swipeclean.domain.usecase

import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.repository.MediaRepository
import javax.inject.Inject

class GetMediaForReview @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(offset: Int, limit: Int = DEFAULT_LIMIT): List<MediaItem> =
        mediaRepository.load(offset = offset, limit = limit)

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
