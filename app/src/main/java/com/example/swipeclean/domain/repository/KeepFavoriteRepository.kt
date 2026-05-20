package com.example.swipeclean.domain.repository

import com.example.swipeclean.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface KeepFavoriteRepository {
    fun observeIds(): Flow<List<Long>>
    suspend fun getIds(): List<Long>
    suspend fun keep(item: MediaItem)
    suspend fun favorite(item: MediaItem)
    suspend fun forget(mediaId: Long)
}
