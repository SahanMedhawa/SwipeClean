package com.example.swipeclean.domain.usecase

import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.repository.MediaRepository
import javax.inject.Inject

class GetMediaForPreview @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(mediaId: Long): MediaItem? = mediaRepository.findById(mediaId)
}
