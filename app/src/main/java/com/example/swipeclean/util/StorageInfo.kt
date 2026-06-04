package com.example.swipeclean.util

import android.content.Context
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Lightweight storage probe — used by the Home dashboard's storage ring.
 *
 * Pulls "total" and "available" bytes from the primary internal volume.
 */
@Singleton
class StorageInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun snapshot(): StorageSnapshot {
        val path = context.filesDir
        val stat = StatFs(path.absolutePath)
        val total = stat.totalBytes
        val free = stat.availableBytes
        val used = (total - free).coerceAtLeast(0)
        return StorageSnapshot(totalBytes = total, usedBytes = used, freeBytes = free)
    }
}

data class StorageSnapshot(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long
)

object ByteFormat {
    fun format(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.lastIndex)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        val rounded = if (value >= 100) {
            "%.0f".format(value)
        } else if (value >= 10) {
            "%.1f".format(value)
        } else {
            "%.2f".format(value)
        }
        return "$rounded ${units[digitGroups]}"
    }
}
