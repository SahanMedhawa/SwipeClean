package com.example.swipeclean.domain.usecase

import android.content.IntentSender
import com.example.swipeclean.domain.repository.BinRepository
import javax.inject.Inject

/**
 * Builds the [IntentSender] required to physically delete the given media ids
 * from MediaStore. The caller must launch this IntentSender from an Activity
 * and call [BinRepository.confirmPermanentlyDeleted] on RESULT_OK.
 */
class PermanentlyDeleteMedia @Inject constructor(
    private val binRepository: BinRepository
) {
    suspend operator fun invoke(mediaIds: List<Long>): IntentSender? =
        binRepository.buildDeleteRequest(mediaIds)
}
