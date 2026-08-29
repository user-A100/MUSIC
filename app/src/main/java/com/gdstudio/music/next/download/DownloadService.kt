package com.gdstudio.music.next.download

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.gdstudio.music.next.MainActivity
import com.gdstudio.music.next.data.AudioTag
import com.gdstudio.music.next.data.AudioTagger
import com.gdstudio.music.next.data.GdMusicApi
import com.gdstudio.music.next.data.HttpFileClient
import com.gdstudio.music.next.data.SourcePreferences
import com.gdstudio.music.next.data.Track
import com.gdstudio.music.next.data.downloadFileName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Sequential download executor, mirroring the website's one-at-a-time batch behaviour.
 *
 * Tasks are pulled from [DownloadStore] as soon as they are queued, so the service owns no
 * duplicate state and the UI can observe the very same list.
 */
class DownloadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var api: GdMusicApi
    private lateinit var files: HttpFileClient
    private lateinit var sink: MediaStoreSink
    private lateinit var notifications: DownloadNotifications
    private var activeJob: Job? = null
    private var activeId: String? = null
    private var startedForeground = false

    override fun onCreate() {
        super.onCreate()
        api = GdMusicApi()
        files = HttpFileClient()
        sink = MediaStoreSink(this)
        notifications = DownloadNotifications(this)
        notifications.createChannel()
        promoteToForeground()
        scope.launch {
            DownloadStore.items.collect { onQueueChanged() }
        }
        scope.launch {
            DownloadStore.items
                .map { items -> items.map { it.status to it.progress } }
                .distinctUntilChanged()
                .collect { notifications.show(DownloadStore.items.value) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                val id = intent.getStringExtra(EXTRA_ID)
                if (id != null && id == activeId) {
                    activeJob?.cancel()
                } else if (id != null) {
                    DownloadStore.update(id) { it.copy(status = DownloadStatus.CANCELED) }
                }
            }
            ACTION_CANCEL_ALL -> {
                DownloadStore.items.value.filter { it.isActive }.forEach {
                    DownloadStore.update(it.id) { item -> item.copy(status = DownloadStatus.CANCELED) }
                }
                activeJob?.cancel()
            }
            else -> Unit
        }
        promoteToForeground()
        onQueueChanged()
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        activeJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun onQueueChanged() {
        if (activeJob?.isActive == true) return
        val next = DownloadStore.items.value.firstOrNull { it.status == DownloadStatus.QUEUED }
        if (next == null) {
            if (DownloadStore.items.value.none { it.isActive }) stopWhenIdle()
            return
        }
        activeId = next.id
        activeJob = scope.launch {
            runDownload(next.id)
            activeId = null
            activeJob = null
            onQueueChanged()
        }
    }

    private suspend fun runDownload(id: String) {
        val item = DownloadStore.get(id) ?: return
        var audioTemp: File? = null
        var taggedTemp: File? = null
        var lyricTemp: File? = null
        try {
            withContext(Dispatchers.IO) {
                DownloadStore.update(id) { it.copy(status = DownloadStatus.RESOLVING, message = null) }
                val sourcePreferences = SourcePreferences(this@DownloadService)
                val hiddenSources = sourcePreferences.loadHidden()
                val fallbackSources = sourcePreferences.loadOrder().filter { it !in hiddenSources }
                val candidate = api.resolveDownload(item.track, item.bitrate, fallbackSources)
                val fileName = downloadFileName(candidate.track, item.nameFormat, candidate.extension)
                DownloadStore.update(id) {
                    it.copy(fileName = fileName, totalBytes = candidate.sizeBytes.coerceAtLeast(0L))
                }

                val temp = File(cacheDir, "gd-audio-$id.tmp").also { audioTemp = it }
                val received = files.download(candidate.url, temp) { done, total ->
                    DownloadStore.update(id) {
                        it.copy(
                            status = DownloadStatus.DOWNLOADING,
                            receivedBytes = done,
                            totalBytes = total.takeIf { value -> value > 0L } ?: it.totalBytes,
                        )
                    }
                }

                DownloadStore.update(id) { it.copy(status = DownloadStatus.FINALIZING) }
                val embed = item.embedMetadata && candidate.extension == "mp3"
                val lyric = if (embed || item.saveLyricFile) api.resolveLyricText(candidate.track) else ""
                val audioFile = if (embed) {
                    val cover = fetchCover(candidate.track)
                    val tagged = File(cacheDir, "gd-tagged-$id.mp3").also { taggedTemp = it }
                    AudioTagger.tagMp3(
                        source = temp,
                        target = tagged,
                        tag = AudioTag(
                            title = candidate.track.name,
                            artist = candidate.track.artistText,
                            album = candidate.track.album,
                            cover = cover?.bytes,
                            coverMime = cover?.mimeType ?: "image/jpeg",
                            lyric = lyric,
                        ),
                    )
                    tagged
                } else {
                    temp
                }

                val uri = sink.saveAudio(audioFile, fileName, candidate.extension, candidate.track)
                if (item.saveLyricFile && lyric.isNotBlank()) {
                    val lyricName = fileName.substringBeforeLast('.') + ".lrc"
                    val lyricFile = File(cacheDir, "gd-lyric-$id.lrc").also { lyricTemp = it }
                    lyricFile.writeText(lyric)
                    sink.saveLyric(lyricFile, lyricName)
                }

                DownloadStore.update(id) {
                    it.copy(
                        status = DownloadStatus.COMPLETED,
                        receivedBytes = received,
                        totalBytes = it.totalBytes.takeIf { value -> value > 0L } ?: received,
                        uri = uri,
                        message = null,
                    )
                }
            }
        } catch (cancelled: CancellationException) {
            DownloadStore.update(id) { it.copy(status = DownloadStatus.CANCELED) }
            throw cancelled
        } catch (error: Throwable) {
            DownloadStore.update(id) {
                it.copy(status = DownloadStatus.FAILED, message = error.message ?: "下载失败")
            }
        } finally {
            audioTemp?.delete()
            taggedTemp?.delete()
            lyricTemp?.delete()
        }
    }

    private suspend fun fetchCover(track: Track) = runCatching {
        val url = api.resolvePicture(track)?.takeIf { it.isNotBlank() } ?: return@runCatching null
        files.fetch(url)
    }.getOrNull()

    private fun promoteToForeground() {
        val notification = notifications.show(DownloadStore.items.value)
        if (startedForeground) {
            notifications.notify(notification)
            return
        }
        ServiceCompat.startForeground(
            this,
            DownloadNotifications.NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
        startedForeground = true
    }

    private fun stopWhenIdle() {
        if (startedForeground) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            startedForeground = false
        }
        stopSelf()
    }

    companion object {
        const val ACTION_PUMP = "com.gdstudio.music.next.download.PUMP"
        const val ACTION_CANCEL = "com.gdstudio.music.next.download.CANCEL"
        const val ACTION_CANCEL_ALL = "com.gdstudio.music.next.download.CANCEL_ALL"
        const val EXTRA_ID = "extra_id"
    }
}

private class DownloadNotifications(private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = android.app.NotificationChannel(
            CHANNEL_ID,
            "音乐下载",
            android.app.NotificationManager.IMPORTANCE_LOW,
        ).apply { description = "显示下载进度与结果" }
        manager.createNotificationChannel(channel)
    }

    /** Updates (or creates) the foreground notification and returns it for callers that need it. */
    fun show(items: List<DownloadItem>): Notification {
        val active = items.filter { it.isActive }
        val completed = items.count { it.status == DownloadStatus.COMPLETED }
        val failed = items.count { it.status == DownloadStatus.FAILED }
        val title = if (active.isNotEmpty()) {
            "正在下载 ${active.size} 首"
        } else {
            buildString {
                append("下载完成 $completed 首")
                if (failed > 0) append("，失败 $failed 首")
            }
        }
        val text = active.firstOrNull()?.let { item ->
            when (item.status) {
                DownloadStatus.QUEUED, DownloadStatus.RESOLVING -> "解析 ${item.track.name}"
                DownloadStatus.FINALIZING -> "写入 ${item.track.name}"
                else -> item.track.name
            }
        } ?: "文件已保存到 音乐/$DOWNLOAD_FOLDER"
        val percent = if (active.isEmpty()) {
            0
        } else {
            (active.sumOf { it.progress.toDouble() } / active.size * 100).toInt().coerceIn(0, 100)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(100, percent, active.isEmpty())
            .setOngoing(active.isNotEmpty())
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent())
            .apply {
                if (active.isNotEmpty()) {
                    addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "全部取消",
                        serviceIntent(DownloadService.ACTION_CANCEL_ALL),
                    )
                }
            }
            .build()
        notify(notification)
        return notification
    }

    fun notify(notification: Notification) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun contentIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun serviceIntent(action: String): PendingIntent = PendingIntent.getService(
        context,
        action.hashCode(),
        Intent(context, DownloadService::class.java).setAction(action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    companion object {
        const val CHANNEL_ID = "gdmusic_downloads"
        const val NOTIFICATION_ID = 2101
    }
}
