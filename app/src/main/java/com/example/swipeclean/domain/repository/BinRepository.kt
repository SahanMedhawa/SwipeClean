package com.example.swipeclean.domain.repository

import android.content.IntentSender
import com.example.swipeclean.domain.model.BinnedMedia
import com.example.swipeclean.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface BinRepository {

    fun observeBin(): Flow<List<BinnedMedia>>

    fun observeBinIds(): Flow<List<Long>>

    fun observeBinCount(): Flow<Int>

    fun observeReclaimableBytes(): Flow<Long>

    suspend fun getAll(): List<BinnedMedia>

    suspend fun sendToBin(item: MediaItem)

    suspend fun restore(mediaId: Long)

    /**
     * Build a single [IntentSender] that, when launched from an Activity,
     * permanently deletes every passed-in media item from the device.
     *
     * The repository does NOT remove rows from Room here; callers should
     * invoke [confirmPermanentlyDeleted] after the IntentSender result
     * comes back with RESULT_OK.
     */
    suspend fun buildDeleteRequest(mediaIds: List<Long>): IntentSender?

    /**
     * Remove rows from the bin table after a successful IntentSender deletion.
     */
    suspend fun confirmPermanentlyDeleted(mediaIds: List<Long>)
}
