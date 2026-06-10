package com.example.swipeclean.domain.usecase.future

import com.example.swipeclean.domain.model.MediaItem

/**
 * Future hook for blurry-photo detection.
 *
 * Implementation plan (not yet shipped):
 *  1. Downscale the source bitmap to ~512px wide.
 *  2. Convert to grayscale.
 *  3. Apply a 3x3 Laplacian kernel and compute the variance of the resulting
 *     pixel intensities. Lower variance ⇒ blurrier image.
 *  4. Anything below a threshold (e.g. 100) is flagged as likely blurry.
 *
 * Surface results as a "Likely blurry" badge on cards so the user can swipe
 * them out faster.
 */
interface BlurDetectionUseCase {
    suspend fun score(item: MediaItem): BlurScore?

    data class BlurScore(val laplacianVariance: Double, val isLikelyBlurry: Boolean)
}

// TODO: Implement
class BlurDetectionUseCaseStub : BlurDetectionUseCase {
    override suspend fun score(item: MediaItem): BlurDetectionUseCase.BlurScore? = null
}
