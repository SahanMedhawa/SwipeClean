package com.example.swipeclean.domain.usecase.future

import com.example.swipeclean.domain.model.MediaItem

/**
 * Future hook for identifying screenshots so they can be auto-grouped into
 * a separate review queue.
 *
 * Implementation plan (not yet shipped):
 *  - Folder heuristic: bucketName == "Screenshots" or contains "screen"
 *  - EXIF heuristic: no camera Make/Model tag + matches screen aspect ratio
 *  - Filename heuristic: starts with "Screenshot_" / "Screen Shot"
 */
interface ScreenshotFilterUseCase {
    suspend fun isScreenshot(item: MediaItem): Boolean
}

// TODO: Implement
class ScreenshotFilterUseCaseStub : ScreenshotFilterUseCase {
    override suspend fun isScreenshot(item: MediaItem): Boolean {
        // A minimal heuristic so the stub isn't useless during development.
        return item.folderName.equals("Screenshots", ignoreCase = true)
    }
}
