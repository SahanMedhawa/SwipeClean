package com.example.swipeclean.data.repository

import com.example.swipeclean.data.local.db.dao.BinDao
import com.example.swipeclean.data.local.db.dao.KeepFavoriteDao
import com.example.swipeclean.data.media.MediaCounts
import com.example.swipeclean.data.media.MediaStoreSource
import com.example.swipeclean.domain.model.MediaFilter
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.repository.MediaRepository
import com.example.swipeclean.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val source: MediaStoreSource,
    private val binDao: BinDao,
    private val keepFavoriteDao: KeepFavoriteDao,
    private val settingsRepository: SettingsRepository
) : MediaRepository {

    override suspend fun load(offset: Int, limit: Int): List<MediaItem> {
        val settings = settingsRepository.current()
        val exclude = buildSet {
            addAll(binDao.getAllIds())
            addAll(keepFavoriteDao.getAllIds())
        }
        return source.load(
            filter = settings.mediaFilter,
            excludeIds = exclude,
            minSizeBytes = settings.minFileSizeBytes,
            offset = offset,
            limit = limit
        )
    }

    override suspend fun counts(filter: MediaFilter): MediaCounts {
        return source.countAll(filter)
    }

    override suspend fun findById(mediaId: Long): MediaItem? = source.findById(mediaId)
}
