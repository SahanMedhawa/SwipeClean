package com.example.swipeclean.domain.usecase

import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.domain.repository.BinRepository
import com.example.swipeclean.domain.repository.KeepFavoriteRepository
import com.example.swipeclean.domain.repository.SwipeHistoryRepository
import javax.inject.Inject

/**
 * Pops the last swipe action for the active session and reverses its effect.
 *
 * Returns the media id that was reverted, so the caller can put it back at
 * the front of the review queue.
 */
class UndoLastAction @Inject constructor(
    private val swipeHistoryRepository: SwipeHistoryRepository,
    private val binRepository: BinRepository,
    private val keepFavoriteRepository: KeepFavoriteRepository
) {
    suspend operator fun invoke(sessionId: Long): UndoResult? {
        val record = swipeHistoryRepository.popLast(sessionId) ?: return null
        when (record.action) {
            SwipeAction.BIN -> binRepository.restore(record.mediaId)
            SwipeAction.KEEP, SwipeAction.FAVORITE -> keepFavoriteRepository.forget(record.mediaId)
        }
        return UndoResult(record.mediaId, record.action)
    }

    data class UndoResult(val mediaId: Long, val action: SwipeAction)
}
