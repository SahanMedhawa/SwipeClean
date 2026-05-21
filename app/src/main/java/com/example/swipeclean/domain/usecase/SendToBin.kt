package com.example.swipeclean.domain.usecase

import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.domain.repository.AnalyticsRepository
import com.example.swipeclean.domain.repository.BinRepository
import com.example.swipeclean.domain.repository.SwipeHistoryRepository
import javax.inject.Inject

class SendToBin @Inject constructor(
    private val binRepository: BinRepository,
    private val swipeHistoryRepository: SwipeHistoryRepository,
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(sessionId: Long, item: MediaItem) {
        binRepository.sendToBin(item)
        swipeHistoryRepository.record(sessionId, item.id, SwipeAction.BIN)
        analyticsRepository.recordSwipe(SwipeAction.BIN, item.folderName, item.sizeBytes)
    }
}
