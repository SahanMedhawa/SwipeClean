package com.example.swipeclean.domain.repository.future

import com.example.swipeclean.domain.model.MediaItem

/**
 * Future hook for an on-device or remote ML model that recommends which media
 * to bin based on content (low-quality, blurry, duplicate, redundant burst).
 *
 * Implementation plan (not yet shipped):
 *  - Wrap a TFLite / MediaPipe classifier behind this interface.
 *  - Expose a flow of suggestions keyed by mediaId.
 *  - The Swipe screen can show a subtle "Suggested" badge or pre-select an
 *     action via the SwipeCard overlay.
 */
interface AISuggestionRepository {
    suspend fun suggest(item: MediaItem): Suggestion?

    enum class Suggestion { BIN, KEEP, FAVORITE }
}

// TODO: Implement
class AISuggestionRepositoryStub : AISuggestionRepository {
    override suspend fun suggest(item: MediaItem): AISuggestionRepository.Suggestion? = null
}
