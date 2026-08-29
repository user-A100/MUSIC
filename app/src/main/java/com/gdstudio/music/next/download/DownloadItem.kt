package com.gdstudio.music.next.download

import android.net.Uri
import com.gdstudio.music.next.data.NameFormat
import com.gdstudio.music.next.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class DownloadStatus {
    QUEUED,
    RESOLVING,
    DOWNLOADING,
    FINALIZING,
    COMPLETED,
    FAILED,
    CANCELED,
}

data class DownloadItem(
    val id: String,
    val track: Track,
    val bitrate: Int,
    val nameFormat: NameFormat,
    val embedMetadata: Boolean,
    val saveLyricFile: Boolean,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val receivedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val fileName: String? = null,
    val uri: Uri? = null,
    val message: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isActive: Boolean
        get() = status == DownloadStatus.QUEUED ||
            status == DownloadStatus.RESOLVING ||
            status == DownloadStatus.DOWNLOADING ||
            status == DownloadStatus.FINALIZING

    val isFinished: Boolean
        get() = status == DownloadStatus.COMPLETED ||
            status == DownloadStatus.FAILED ||
            status == DownloadStatus.CANCELED

    val progress: Float
        get() = when {
            status == DownloadStatus.COMPLETED -> 1f
            totalBytes > 0L -> (receivedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
            else -> 0f
        }
}

/**
 * Process-wide download queue.
 *
 * The [DownloadService] owns execution and only mutates items it currently runs, while the UI
 * observes the same list, so no binder or database is needed to keep both in sync.
 */
object DownloadStore {
    private val mutableItems = MutableStateFlow<List<DownloadItem>>(emptyList())
    val items: StateFlow<List<DownloadItem>> = mutableItems.asStateFlow()

    fun enqueue(items: List<DownloadItem>) {
        mutableItems.update { current -> items + current }
    }

    fun get(id: String): DownloadItem? = mutableItems.value.firstOrNull { it.id == id }

    fun update(id: String, transform: (DownloadItem) -> DownloadItem) {
        mutableItems.update { current ->
            current.map { if (it.id == id) transform(it) else it }
        }
    }

    fun remove(id: String) {
        mutableItems.update { current -> current.filterNot { it.id == id } }
    }

    fun clearFinished() {
        mutableItems.update { current -> current.filterNot { it.isFinished } }
    }
}
