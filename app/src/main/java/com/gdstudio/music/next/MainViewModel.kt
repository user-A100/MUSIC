package com.gdstudio.music.next

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.gdstudio.music.next.data.DownloadOptions
import com.gdstudio.music.next.data.GdMusicApi
import com.gdstudio.music.next.data.LyricLine
import com.gdstudio.music.next.data.LibraryRepository
import com.gdstudio.music.next.data.LibraryState
import com.gdstudio.music.next.data.LocalMusicRepository
import com.gdstudio.music.next.data.MusicSource
import com.gdstudio.music.next.data.PlaybackMode
import com.gdstudio.music.next.data.PlaybackOptions
import com.gdstudio.music.next.data.PlaybackPreferences
import com.gdstudio.music.next.data.PlaybackSession
import com.gdstudio.music.next.data.PlaybackSessionRepository
import com.gdstudio.music.next.data.next
import com.gdstudio.music.next.data.NameFormat
import com.gdstudio.music.next.data.ResolvedTrack
import com.gdstudio.music.next.data.SourcePreferences
import com.gdstudio.music.next.data.Track
import com.gdstudio.music.next.data.parseLrc
import com.gdstudio.music.next.download.DownloadItem
import com.gdstudio.music.next.download.DownloadPermissions
import com.gdstudio.music.next.download.DownloadScheduler
import com.gdstudio.music.next.download.DownloadStore
import com.gdstudio.music.next.playback.PlaybackService
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class MusicUiState(
    val query: String = "",
    val selectedSource: MusicSource = MusicSource.NETEASE,
    val results: List<Track> = emptyList(),
    val queue: List<Track> = emptyList(),
    val current: ResolvedTrack? = null,
    val lyricLines: List<LyricLine> = emptyList(),
    val isSearching: Boolean = false,
    val isResolving: Boolean = false,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bitrate: Int = 320,
    val playbackMode: PlaybackMode = PlaybackMode.SEQUENTIAL,
    val shuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1f,
    val sourceOrder: List<MusicSource> = MusicSource.defaultPriority,
    val hiddenSources: Set<MusicSource> = emptySet(),
    val downloadOptions: DownloadOptions = DownloadOptions(),
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val restorableTrack: Track? = null,
    val restorablePositionMs: Long = 0L,
    val localTracks: List<Track> = emptyList(),
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sourcePreferences = SourcePreferences(application)
    private val playbackPreferences = PlaybackPreferences(application)
    private val playbackSessionRepository = PlaybackSessionRepository(application)
    private val initialSession = playbackSessionRepository.load()
    private val initialPlaybackOptions = playbackPreferences.load()
    private val initialSourceOrder = sourcePreferences.loadOrder()
    private val initialHiddenSources = sourcePreferences.loadHidden()
    private val api = GdMusicApi()
    private val libraryRepository = LibraryRepository(application)
    private val localMusicRepository = LocalMusicRepository(application)
    private var player: MediaController? = null
    private val controllerFuture = MediaController.Builder(
        application,
        SessionToken(application, ComponentName(application, PlaybackService::class.java)),
    ).buildAsync()
    private val mutableState = MutableStateFlow(
        MusicUiState(
            selectedSource = initialSourceOrder.firstOrNull { it !in initialHiddenSources } ?: MusicSource.NETEASE,
            sourceOrder = initialSourceOrder,
            hiddenSources = initialHiddenSources,
            playbackMode = initialPlaybackOptions.mode,
            shuffleEnabled = initialPlaybackOptions.shuffleEnabled,
            playbackSpeed = initialPlaybackOptions.speed,
            queue = initialSession.queue,
            restorableTrack = initialSession.currentTrack,
            restorablePositionMs = initialSession.positionMs,
        ),
    )
    val state: StateFlow<MusicUiState> = mutableState.asStateFlow()
    val downloads: StateFlow<List<DownloadItem>> = DownloadStore.items
    val library: StateFlow<LibraryState> = libraryRepository.state
    private var sleepTimerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            mutableState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) updateProgress()
            if (playbackState == Player.STATE_ENDED) handlePlaybackEnded()
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            mutableState.update {
                it.copy(errorMessage = error.localizedMessage ?: "播放失败，请尝试其他音乐源")
            }
        }
    }

    init {
        controllerFuture.addListener({
            player = runCatching { controllerFuture.get() }.getOrNull()?.also { controller ->
                controller.addListener(playerListener)
                applyRepeatMode(controller, mutableState.value.playbackMode)
                controller.setPlaybackSpeed(mutableState.value.playbackSpeed)
                updateProgress()
            }
        }, ContextCompat.getMainExecutor(application))
        viewModelScope.launch {
            while (isActive) {
                updateProgress()
                delay(500L)
            }
        }
    }

    fun updateQuery(value: String) {
        mutableState.update { it.copy(query = value) }
    }

    fun selectSource(source: MusicSource) {
        mutableState.update { it.copy(selectedSource = source, results = emptyList()) }
    }

    fun moveSource(source: MusicSource, offset: Int) {
        mutableState.update { state ->
            val currentIndex = state.sourceOrder.indexOf(source)
            val targetIndex = (currentIndex + offset).coerceIn(0, state.sourceOrder.lastIndex)
            if (currentIndex < 0 || currentIndex == targetIndex) return@update state
            val reordered = state.sourceOrder.toMutableList().apply {
                add(targetIndex, removeAt(currentIndex))
            }
            sourcePreferences.save(reordered, state.hiddenSources)
            state.copy(sourceOrder = reordered)
        }
    }

    fun setSourceVisible(source: MusicSource, visible: Boolean) {
        mutableState.update { state ->
            val visibleCount = state.sourceOrder.count { it !in state.hiddenSources }
            if (!visible && visibleCount <= 1) {
                return@update state.copy(errorMessage = "至少保留一个可见音源")
            }
            val hidden = state.hiddenSources.toMutableSet().apply {
                if (visible) remove(source) else add(source)
            }
            val selected = if (state.selectedSource in hidden) {
                state.sourceOrder.first { it !in hidden }
            } else {
                state.selectedSource
            }
            sourcePreferences.save(state.sourceOrder, hidden)
            state.copy(hiddenSources = hidden, selectedSource = selected)
        }
    }

    fun setBitrate(value: Int) {
        mutableState.update { it.copy(bitrate = value) }
    }

    fun search() {
        val snapshot = mutableState.value
        if (snapshot.query.isBlank() || snapshot.isSearching) return
        viewModelScope.launch {
            mutableState.update { it.copy(isSearching = true, errorMessage = null) }
            runCatching { api.search(snapshot.query, snapshot.selectedSource) }
                .onSuccess { tracks ->
                    mutableState.update { it.copy(results = tracks, isSearching = false) }
                }
                .onFailure { error ->
                    mutableState.update {
                        it.copy(
                            isSearching = false,
                            errorMessage = error.localizedMessage ?: "搜索失败，请稍后重试",
                        )
                    }
                }
        }
    }

    fun play(track: Track) = play(track, 0L)

    private fun play(track: Track, startPositionMs: Long) {
        if (mutableState.value.isResolving) return
        viewModelScope.launch {
            mutableState.update { current ->
                current.copy(
                    isResolving = true,
                    errorMessage = null,
                    queue = if (track in current.queue) current.queue else current.queue + track,
                )
            }
            try {
                val sourceState = mutableState.value
                val fallbacks = sourceState.sourceOrder.filter { it !in sourceState.hiddenSources }
                val resolved = if (track.source == LocalMusicRepository.LOCAL_SOURCE) {
                    ResolvedTrack(track, track.urlId, null, "", 0)
                } else {
                    api.resolve(track, sourceState.bitrate, fallbacks)
                }
                val metadata = MediaMetadata.Builder()
                    .setTitle(resolved.track.name)
                    .setArtist(resolved.track.artistText)
                    .setAlbumTitle(resolved.track.album)
                    .apply { resolved.artworkUrl?.let { setArtworkUri(it.toUri()) } }
                    .build()
                val item = MediaItem.Builder()
                    .setUri(resolved.audioUrl)
                    .setMediaId("${resolved.track.source}:${resolved.track.id}")
                    .setMediaMetadata(metadata)
                    .build()
                val controller = awaitPlayer()
                controller.setMediaItem(item)
                controller.prepare()
                if (startPositionMs > 0L) controller.seekTo(startPositionMs)
                controller.setPlaybackSpeed(mutableState.value.playbackSpeed)
                controller.play()
                mutableState.update {
                    it.copy(
                        current = resolved,
                        queue = it.queue.map { queued -> if (queued == track) resolved.track else queued },
                        lyricLines = parseLrc(resolved.lyric),
                        isResolving = false,
                        bitrate = resolved.bitrate,
                        restorableTrack = null,
                        restorablePositionMs = 0L,
                    )
                }
                libraryRepository.recordPlayed(resolved.track)
                savePlaybackSession()
            } catch (error: Throwable) {
                mutableState.update {
                    it.copy(
                        isResolving = false,
                        errorMessage = error.localizedMessage ?: "无法获取播放地址",
                    )
                }
            }
        }
    }

    fun addToQueue(track: Track) {
        mutableState.update { current ->
            if (track in current.queue) current else current.copy(queue = current.queue + track)
        }
        savePlaybackSession()
    }

    fun playNextInQueue(track: Track) {
        mutableState.update { state ->
            val without = state.queue.filterNot { it.source == track.source && it.id == track.id }
            val current = state.current?.track ?: state.restorableTrack
            val index = without.indexOfFirst { it.source == current?.source && it.id == current.id }
            val insertion = if (index >= 0) index + 1 else 0
            state.copy(queue = without.toMutableList().apply { add(insertion, track) }, statusMessage = "已设为下一首播放")
        }
        savePlaybackSession()
    }

    fun removeFromQueue(track: Track) {
        mutableState.update { it.copy(queue = it.queue - track) }
        savePlaybackSession()
    }

    fun moveQueueItem(fromIndex: Int, offset: Int) {
        mutableState.update { state ->
            val target = (fromIndex + offset).coerceIn(0, state.queue.lastIndex)
            if (fromIndex !in state.queue.indices || target == fromIndex) return@update state
            state.copy(queue = state.queue.toMutableList().apply { add(target, removeAt(fromIndex)) })
        }
        savePlaybackSession()
    }

    fun clearQueue() {
        mutableState.update { it.copy(queue = it.current?.track?.let(::listOf) ?: emptyList()) }
        savePlaybackSession()
    }

    fun resumeLastSession() {
        val snapshot = mutableState.value
        snapshot.restorableTrack?.let { play(it, snapshot.restorablePositionMs) }
    }

    fun setPlaybackSpeed(speed: Float) {
        val bounded = speed.coerceIn(0.5f, 2f)
        player?.setPlaybackSpeed(bounded)
        mutableState.update { it.copy(playbackSpeed = bounded, statusMessage = "播放速度 ${bounded}x") }
        savePlaybackOptions()
    }

    fun toggleLiked(track: Track) = libraryRepository.toggleLiked(track)

    fun createPlaylist(name: String): String? = libraryRepository.createPlaylist(name)

    fun renamePlaylist(id: String, name: String): Boolean = libraryRepository.renamePlaylist(id, name)

    fun deletePlaylist(playlistId: String) = libraryRepository.deletePlaylist(playlistId)

    fun addToPlaylist(playlistId: String, track: Track) {
        val added = libraryRepository.addToPlaylist(playlistId, track)
        mutableState.update {
            it.copy(statusMessage = if (added) "已加入歌单" else "歌曲已在歌单中")
        }
    }

    fun removeFromPlaylist(playlistId: String, track: Track) =
        libraryRepository.removeFromPlaylist(playlistId, track)

    fun clearHistory() = libraryRepository.clearHistory()

    fun scanLocalMusic() {
        viewModelScope.launch {
            runCatching { localMusicRepository.scan() }
                .onSuccess { tracks -> mutableState.update { it.copy(localTracks = tracks, statusMessage = "已扫描 ${tracks.size} 首本地音乐") } }
                .onFailure { error -> mutableState.update { it.copy(errorMessage = error.localizedMessage ?: "无法读取本地音乐") } }
        }
    }

    fun togglePlayback() {
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun playPrevious() {
        val snapshot = mutableState.value
        val currentTrack = snapshot.current?.track ?: return
        val index = snapshot.queue.indexOf(currentTrack)
        if (index > 0) play(snapshot.queue[index - 1]) else player?.seekTo(0L)
    }

    fun playNext() {
        val snapshot = mutableState.value
        val currentTrack = snapshot.current?.track ?: return
        chooseNextTrack(snapshot, currentTrack, wrap = snapshot.playbackMode == PlaybackMode.REPEAT_ALL)?.let(::play)
    }

    fun cyclePlaybackMode() {
        val next = mutableState.value.playbackMode.next()
        mutableState.update { it.copy(playbackMode = next, statusMessage = next.label) }
        applyRepeatMode(player, next)
        savePlaybackOptions()
    }

    fun toggleShuffle() {
        mutableState.update {
            val enabled = !it.shuffleEnabled
            it.copy(shuffleEnabled = enabled, statusMessage = if (enabled) "随机播放已开启" else "随机播放已关闭")
        }
        savePlaybackOptions()
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs.coerceAtLeast(0L))
        updateProgress()
    }

    /** Stops playback after the selected period. Passing null cancels an active timer. */
    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        if (minutes == null) {
            mutableState.update { it.copy(statusMessage = "睡眠定时已取消") }
            return
        }
        sleepTimerJob = viewModelScope.launch {
            mutableState.update { it.copy(statusMessage = "将在 $minutes 分钟后停止播放") }
            delay(minutes * 60_000L)
            player?.pause()
            mutableState.update { it.copy(statusMessage = "睡眠定时已停止播放") }
        }
    }

    fun clearError() {
        mutableState.update { it.copy(errorMessage = null) }
    }

    fun setDownloadBitrate(value: Int) {
        mutableState.update { it.copy(downloadOptions = it.downloadOptions.copy(bitrate = value)) }
    }

    fun setDownloadNameFormat(format: NameFormat) {
        mutableState.update { it.copy(downloadOptions = it.downloadOptions.copy(nameFormat = format)) }
    }

    fun setEmbedMetadata(enabled: Boolean) {
        mutableState.update { it.copy(downloadOptions = it.downloadOptions.copy(embedMetadata = enabled)) }
    }

    fun setSaveLyricFile(enabled: Boolean) {
        mutableState.update { it.copy(downloadOptions = it.downloadOptions.copy(saveLyricFile = enabled)) }
    }

    /** Single track download, the direct counterpart of the website's per-row download icon. */
    fun download(track: Track) = enqueueDownloads(listOf(track))

    /** Batch download, capped at 20 tracks just like the website's batch dialog. */
    fun downloadBatch(tracks: List<Track>, range: IntRange) {
        val bounded = range.first.coerceAtLeast(0)..range.last.coerceAtMost(tracks.lastIndex)
        enqueueDownloads(tracks.slice(bounded))
    }

    fun cancelDownload(id: String) = DownloadScheduler.cancel(getApplication(), id)

    fun retryDownload(id: String) = DownloadScheduler.retry(getApplication(), id)

    fun removeDownload(id: String) = DownloadStore.remove(id)

    fun clearFinishedDownloads() = DownloadStore.clearFinished()

    private fun enqueueDownloads(tracks: List<Track>) {
        val tracksToDownload = tracks.take(MAX_BATCH_DOWNLOADS)
        if (tracksToDownload.isEmpty()) return
        val application = getApplication<Application>()
        if (!DownloadPermissions.isGranted(application)) {
            mutableState.update { it.copy(errorMessage = "下载需要通知权限，用于在通知栏显示进度") }
            return
        }
        val options = mutableState.value.downloadOptions
        val items = tracksToDownload.map { track ->
            DownloadItem(
                id = UUID.randomUUID().toString(),
                track = track,
                bitrate = options.bitrate,
                nameFormat = options.nameFormat,
                embedMetadata = options.embedMetadata,
                saveLyricFile = options.saveLyricFile,
            )
        }
        DownloadScheduler.enqueue(application, items)
        mutableState.update { it.copy(errorMessage = null, statusMessage = "已加入下载队列：${items.size} 首") }
    }

    fun clearStatusMessage() {
        mutableState.update { it.copy(statusMessage = null) }
    }

    private fun updateProgress() {
        val player = player ?: return
        val duration = player.duration.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
        mutableState.update {
            it.copy(
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = duration,
                isPlaying = player.isPlaying,
            )
        }
        if (player.currentPosition % 3_000L < 600L) savePlaybackSession()
    }

    private fun handlePlaybackEnded() {
        val snapshot = mutableState.value
        val currentTrack = snapshot.current?.track ?: return
        if (snapshot.playbackMode == PlaybackMode.REPEAT_ONE) return
        chooseNextTrack(snapshot, currentTrack, wrap = snapshot.playbackMode == PlaybackMode.REPEAT_ALL)?.let(::play)
    }

    private fun chooseNextTrack(state: MusicUiState, currentTrack: Track, wrap: Boolean): Track? {
        if (state.queue.isEmpty()) return null
        if (state.shuffleEnabled) {
            val candidates = state.queue.filterNot { it.source == currentTrack.source && it.id == currentTrack.id }
            return candidates.randomOrNull(Random.Default) ?: if (wrap) currentTrack else null
        }
        val index = state.queue.indexOfFirst { it.source == currentTrack.source && it.id == currentTrack.id }
        return when {
            index in 0 until state.queue.lastIndex -> state.queue[index + 1]
            wrap -> state.queue.firstOrNull()
            else -> null
        }
    }

    private fun applyRepeatMode(controller: MediaController?, mode: PlaybackMode) {
        controller?.repeatMode = if (mode == PlaybackMode.REPEAT_ONE) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    private fun savePlaybackOptions() {
        val snapshot = mutableState.value
        playbackPreferences.save(PlaybackOptions(snapshot.playbackMode, snapshot.shuffleEnabled, snapshot.playbackSpeed))
    }

    private fun savePlaybackSession() {
        val snapshot = mutableState.value
        playbackSessionRepository.save(
            PlaybackSession(snapshot.queue, snapshot.current?.track ?: snapshot.restorableTrack, snapshot.positionMs),
        )
    }

    private companion object {
        /** The website's batch dialog never resolves more than 20 links at a time. */
        const val MAX_BATCH_DOWNLOADS = 20
    }

    override fun onCleared() {
        sleepTimerJob?.cancel()
        player?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
        super.onCleared()
    }

    private suspend fun awaitPlayer(): MediaController {
        player?.let { return it }
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            controllerFuture.get()
        }.also { player = it }
    }
}
