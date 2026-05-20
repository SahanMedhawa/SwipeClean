package com.example.swipeclean.domain.repository

import com.example.swipeclean.domain.model.SwipeAction

interface SwipeHistoryRepository {
    /**
     * The session id is generated when the user enters the swipe screen and
     * reused for every action until they leave. Undo only operates on the
     * current session.
     */
    fun startSession(): Long

    suspend fun record(sessionId: Long, mediaId: Long, action: SwipeAction)

    suspend fun lastAction(sessionId: Long): RecordedAction?

    suspend fun popLast(sessionId: Long): RecordedAction?

    /** Drop everything except rows for [sessionId]. Call when entering a new session. */
    suspend fun clearOtherSessions(sessionId: Long)

    data class RecordedAction(
        val id: Long,
        val mediaId: Long,
        val action: SwipeAction,
        val timestamp: Long
    )
}
