package com.example.swipeclean.data.repository

import com.example.swipeclean.data.local.db.dao.SwipeActionDao
import com.example.swipeclean.data.local.db.entities.SwipeActionEntity
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.domain.repository.SwipeHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwipeHistoryRepositoryImpl @Inject constructor(
    private val dao: SwipeActionDao
) : SwipeHistoryRepository {

    override fun startSession(): Long = System.currentTimeMillis()

    override suspend fun record(sessionId: Long, mediaId: Long, action: SwipeAction) {
        dao.insert(
            SwipeActionEntity(
                sessionId = sessionId,
                mediaId = mediaId,
                action = action.name,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun lastAction(sessionId: Long): SwipeHistoryRepository.RecordedAction? {
        val row = dao.lastForSession(sessionId) ?: return null
        return row.toRecorded()
    }

    override suspend fun popLast(sessionId: Long): SwipeHistoryRepository.RecordedAction? {
        val row = dao.lastForSession(sessionId) ?: return null
        dao.deleteById(row.id)
        return row.toRecorded()
    }

    override suspend fun clearOtherSessions(sessionId: Long) {
        dao.clearAllExceptSession(sessionId)
    }

    private fun SwipeActionEntity.toRecorded() = SwipeHistoryRepository.RecordedAction(
        id = id,
        mediaId = mediaId,
        action = runCatching { SwipeAction.valueOf(action) }.getOrDefault(SwipeAction.KEEP),
        timestamp = timestamp
    )
}
