package com.example.swipeclean.domain.repository.future

import com.example.swipeclean.domain.model.MediaItem

/**
 * Future hook for cloud-backup migration (Google Drive, iCloud, OneDrive…).
 *
 * Implementation plan (not yet shipped):
 *  - Concrete implementations would back this with provider SDKs and surface
 *    upload progress as a Flow.
 *  - The Settings screen could expose "Back up before binning" toggles.
 */
interface CloudBackupRepository {
    suspend fun isBackedUp(item: MediaItem): Boolean
    suspend fun backUp(item: MediaItem): Result<Unit>
    suspend fun listProviders(): List<String>
}

// TODO: Implement
class CloudBackupRepositoryStub : CloudBackupRepository {
    override suspend fun isBackedUp(item: MediaItem): Boolean = false
    override suspend fun backUp(item: MediaItem): Result<Unit> =
        Result.failure(UnsupportedOperationException("Cloud backup not yet implemented"))
    override suspend fun listProviders(): List<String> = emptyList()
}
