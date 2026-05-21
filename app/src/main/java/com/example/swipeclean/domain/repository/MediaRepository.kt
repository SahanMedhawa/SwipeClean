package com.example.swipeclean.domain.repository

import com.example.swipeclean.data.media.MediaCounts
import com.example.swipeclean.domain.model.MediaFilter
import com.example.swipeclean.domain.model.MediaItem

interface MediaRepository {
    /**
     * Pull a window of media starting at [offset], up to [limit] items,
     * excluding any that are already in the bin or in keep/favorite.
     */
    suspend fun load(offset: Int, limit: Int): List<MediaItem>

    /** Photo + video totals (unfiltered by exclusion set). */
    suspend fun counts(filter: MediaFilter = MediaFilter.BOTH): MediaCounts

    /** Resolve a single item by id for the preview screen. */
    suspend fun findById(mediaId: Long): MediaItem?
}
