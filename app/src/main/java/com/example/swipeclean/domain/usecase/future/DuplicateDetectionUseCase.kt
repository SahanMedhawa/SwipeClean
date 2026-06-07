package com.example.swipeclean.domain.usecase.future

import com.example.swipeclean.domain.model.MediaItem

/**
 * Future hook for perceptual-hash (pHash) based duplicate detection.
 *
 * Implementation plan (not yet shipped):
 *  1. For every image in the review window, compute a 64-bit pHash off the
 *     thumbnail (downscale to 32x32, DCT, threshold against mean).
 *  2. Cache hashes keyed by mediaId in a new Room table.
 *  3. Cluster items whose Hamming distance is below a configurable threshold
 *     (default 10) and surface a "DUPLICATE" badge on the card.
 *
 * The Swipe screen already renders the DUPLICATE badge slot; wire this in
 * once the implementation lands.
 */
interface DuplicateDetectionUseCase {
    suspend fun isDuplicate(item: MediaItem): Boolean

    suspend fun findClusters(items: List<MediaItem>): List<DuplicateCluster>

    data class DuplicateCluster(val representativeId: Long, val memberIds: List<Long>)
}

// TODO: Implement
class DuplicateDetectionUseCaseStub : DuplicateDetectionUseCase {
    override suspend fun isDuplicate(item: MediaItem): Boolean = false
    override suspend fun findClusters(items: List<MediaItem>): List<DuplicateDetectionUseCase.DuplicateCluster> = emptyList()
}
