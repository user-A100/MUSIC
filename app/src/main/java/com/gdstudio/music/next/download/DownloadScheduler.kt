package com.gdstudio.music.next.download

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/** Entry point used by the UI to hand work to [DownloadService]. */
object DownloadScheduler {
    fun enqueue(context: Context, items: List<DownloadItem>) {
        DownloadStore.enqueue(items)
        pump(context)
    }

    private fun pump(context: Context) {
        ContextCompat.startForegroundService(
            context,
            Intent(context, DownloadService::class.java).setAction(DownloadService.ACTION_PUMP),
        )
    }

    fun cancel(context: Context, id: String) {
        DownloadStore.update(id) { it.copy(status = DownloadStatus.CANCELED, message = null) }
        ContextCompat.startForegroundService(
            context,
            Intent(context, DownloadService::class.java)
                .setAction(DownloadService.ACTION_CANCEL)
                .putExtra(DownloadService.EXTRA_ID, id),
        )
    }

    fun retry(context: Context, id: String) {
        DownloadStore.update(id) {
            it.copy(
                status = DownloadStatus.QUEUED,
                receivedBytes = 0L,
                totalBytes = 0L,
                message = null,
            )
        }
        pump(context)
    }
}
