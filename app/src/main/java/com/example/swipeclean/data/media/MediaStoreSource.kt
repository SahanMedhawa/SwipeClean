package com.example.swipeclean.data.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.swipeclean.domain.model.MediaFilter
import com.example.swipeclean.domain.model.MediaItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thin wrapper over MediaStore that exposes paged, filtered queries for both
 * images and videos via the unified Files table.
 *
 * We query [MediaStore.Files.getContentUri] (with the "external" volume) and
 * use [MediaStore.Files.FileColumns.MEDIA_TYPE] to discriminate photos vs videos.
 * This avoids running two separate queries / cursors.
 */
@Singleton
class MediaStoreSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Loads up to [limit] items, skipping [offset], filtered per [filter].
     * Excludes any ids in [excludeIds] (binned / kept / favorited media).
     *
     * Items below [minSizeBytes] are skipped. Returned items are ordered by
     * date taken descending so the user sees the newest content first.
     */
    suspend fun load(
        filter: MediaFilter,
        excludeIds: Set<Long>,
        minSizeBytes: Long,
        offset: Int,
        limit: Int
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.DURATION,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val typeSelection = when (filter) {
            MediaFilter.PHOTOS_ONLY -> "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
            MediaFilter.VIDEOS_ONLY -> "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
            MediaFilter.BOTH -> "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)"
        }
        val typeArgs = when (filter) {
            MediaFilter.PHOTOS_ONLY -> arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
            MediaFilter.VIDEOS_ONLY -> arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
            MediaFilter.BOTH -> arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
        }
        val sizeSelection = if (minSizeBytes > 0) " AND ${MediaStore.Files.FileColumns.SIZE} >= ?" else ""
        val selection = typeSelection + sizeSelection
        val selectionArgs = if (minSizeBytes > 0) typeArgs + minSizeBytes.toString() else typeArgs

        val sortOrder =
            "COALESCE(${MediaStore.Files.FileColumns.DATE_TAKEN}, ${MediaStore.Files.FileColumns.DATE_ADDED} * 1000) DESC"

        val collection = MediaStore.Files.getContentUri("external")

        val results = mutableListOf<MediaItem>()
        // We over-fetch (offset + limit + buffer) and apply the exclude-set in memory
        // because MediaStore cursors don't accept arbitrarily long NOT IN clauses.
        val window = (offset + limit + excludeIds.size).coerceAtLeast(limit)

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
            val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            var skipped = 0
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                if (id in excludeIds) continue
                if (skipped < offset) {
                    skipped++
                    continue
                }
                val mediaType = cursor.getInt(typeCol)
                val isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val uri = if (isVideo) {
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                } else {
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                }
                val dateTaken = cursor.getLong(dateTakenCol).takeIf { it > 0 }
                    ?: (cursor.getLong(dateAddedCol) * 1000L)

                results += MediaItem(
                    id = id,
                    uri = uri,
                    displayName = cursor.getString(nameCol) ?: "Untitled",
                    mimeType = cursor.getString(mimeCol) ?: if (isVideo) "video/*" else "image/*",
                    sizeBytes = cursor.getLong(sizeCol),
                    width = cursor.getInt(widthCol),
                    height = cursor.getInt(heightCol),
                    durationMs = if (isVideo) cursor.getLong(durationCol).takeIf { it > 0 } else null,
                    dateTakenMs = dateTaken,
                    folderName = cursor.getString(bucketCol) ?: "Camera",
                    isVideo = isVideo
                )
                if (results.size >= limit || results.size >= window) break
            }
        }
        results
    }

    /**
     * Look up a single media row by id. Returns null if the row no longer exists.
     */
    suspend fun findById(mediaId: Long): MediaItem? = withContext(Dispatchers.IO) {
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.DURATION,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )
        context.contentResolver.query(
            collection,
            projection,
            "${MediaStore.Files.FileColumns._ID}=?",
            arrayOf(mediaId.toString()),
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val isVideo = cursor.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            val uri = if (isVideo) {
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId)
            } else {
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId)
            }
            val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val dateTaken = cursor.getLong(dateTakenCol).takeIf { it > 0 }
                ?: (cursor.getLong(dateAddedCol) * 1000L)
            MediaItem(
                id = mediaId,
                uri = uri,
                displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
                    ?: "Untitled",
                mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
                    ?: if (isVideo) "video/*" else "image/*",
                sizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)),
                width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)),
                height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)),
                durationMs = if (isVideo) cursor.getLong(
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
                ).takeIf { it > 0 } else null,
                dateTakenMs = dateTaken,
                folderName = cursor.getString(
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                ) ?: "Camera",
                isVideo = isVideo
            )
        }
    }

    suspend fun countAll(filter: MediaFilter): MediaCounts = withContext(Dispatchers.IO) {
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE)
        val selection = "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)"
        val args = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        var photos = 0
        var videos = 0
        context.contentResolver.query(collection, projection, selection, args, null)?.use { cursor ->
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            while (cursor.moveToNext()) {
                if (cursor.getInt(typeCol) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) videos++
                else photos++
            }
        }
        when (filter) {
            MediaFilter.PHOTOS_ONLY -> MediaCounts(photos, 0)
            MediaFilter.VIDEOS_ONLY -> MediaCounts(0, videos)
            MediaFilter.BOTH -> MediaCounts(photos, videos)
        }
    }
}

data class MediaCounts(val photos: Int, val videos: Int) {
    val total: Int get() = photos + videos
}
