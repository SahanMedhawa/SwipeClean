package com.example.swipeclean.data.repository

import com.example.swipeclean.data.local.db.dao.KeepFavoriteDao
import com.example.swipeclean.data.local.db.entities.KeepFavoriteEntity
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.repository.KeepFavoriteRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class KeepFavoriteRepositoryImpl @Inject constructor(
    private val dao: KeepFavoriteDao
) : KeepFavoriteRepository {

    override fun observeIds(): Flow<List<Long>> = dao.observeAllIds()

    override suspend fun getIds(): List<Long> = dao.getAllIds()

    override suspend fun keep(item: MediaItem) {
        dao.upsert(KeepFavoriteEntity(item.id, isFavorite = false, recordedAt = System.currentTimeMillis()))
    }

    override suspend fun favorite(item: MediaItem) {
        dao.upsert(KeepFavoriteEntity(item.id, isFavorite = true, recordedAt = System.currentTimeMillis()))
    }

    override suspend fun forget(mediaId: Long) {
        dao.deleteById(mediaId)
    }
}
