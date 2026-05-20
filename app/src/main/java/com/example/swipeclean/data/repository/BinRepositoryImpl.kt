package com.example.swipeclean.data.repository

import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.swipeclean.data.local.db.dao.BinDao
import com.example.swipeclean.data.local.db.entities.BinnedMediaEntity
import com.example.swipeclean.domain.model.BinnedMedia
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.repository.BinRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BinRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val binDao: BinDao
) : BinRepository {

    override fun observeBin(): Flow<List<BinnedMedia>> =
        binDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeBinIds(): Flow<List<Long>> = binDao.observeAllIds()

    override fun observeBinCount(): Flow<Int> = binDao.observeCount()

    override fun observeReclaimableBytes(): Flow<Long> = binDao.observeTotalSize()

    override suspend fun getAll(): List<BinnedMedia> = binDao.getAll().map { it.toDomain() }

    override suspend fun sendToBin(item: MediaItem) {
        binDao.insert(
            BinnedMediaEntity(
                mediaId = item.id,
                uri = item.uri.toString(),
                displayName = item.displayName,
                mimeType = item.mimeType,
                sizeBytes = item.sizeBytes,
                width = item.width,
                height = item.height,
                duration = item.durationMs,
                dateTaken = item.dateTakenMs,
                folderName = item.folderName,
                binnedAt = System.currentTimeMillis(),
                thumbnailPath = null,
                isVideo = item.isVideo
            )
        )
    }

    override suspend fun restore(mediaId: Long) {
        binDao.deleteById(mediaId)
    }

    override suspend fun buildDeleteRequest(mediaIds: List<Long>): IntentSender? {
        if (mediaIds.isEmpty()) return null
        val uris: List<Uri> = mediaIds.mapNotNull { id ->
            val entity = binDao.getById(id) ?: return@mapNotNull null
            runCatching { Uri.parse(entity.uri) }.getOrNull()
        }
        if (uris.isEmpty()) return null

        // createDeleteRequest is API 30+; we're minSdk 31 so always available.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
        } else {
            null
        }
    }

    override suspend fun confirmPermanentlyDeleted(mediaIds: List<Long>) {
        binDao.deleteByIds(mediaIds)
    }
}

private fun BinnedMediaEntity.toDomain(): BinnedMedia = BinnedMedia(
    mediaId = mediaId,
    uri = Uri.parse(uri),
    displayName = displayName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    width = width,
    height = height,
    durationMs = duration,
    dateTakenMs = dateTaken,
    folderName = folderName,
    binnedAt = binnedAt,
    isVideo = isVideo
)
