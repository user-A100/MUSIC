package com.gdstudio.music.next.ui

import android.content.Intent
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.gdstudio.music.next.MainViewModel
import com.gdstudio.music.next.MusicUiState
import com.gdstudio.music.next.data.DownloadOptions
import com.gdstudio.music.next.data.LyricLine
import com.gdstudio.music.next.data.LibraryState
import com.gdstudio.music.next.data.LocalPlaylist
import com.gdstudio.music.next.data.MusicSource
import com.gdstudio.music.next.data.NameFormat
import com.gdstudio.music.next.data.PlaybackMode
import com.gdstudio.music.next.data.SourceAvailability
import com.gdstudio.music.next.data.Track
import com.gdstudio.music.next.data.libraryKey
import com.gdstudio.music.next.download.DOWNLOAD_FOLDER
import com.gdstudio.music.next.download.DownloadItem
import com.gdstudio.music.next.download.DownloadStatus
import com.gdstudio.music.next.download.mimeTypeOf
import com.gdstudio.music.next.ui.theme.AppThemeMode
import com.gdstudio.music.next.ui.theme.SeedColor
import com.gdstudio.music.next.ui.theme.ThemeSettings

private enum class Destination(
    val label: String,
    val icon: ImageVector,
) {
    HOME("首页", Icons.Rounded.Home),
    SEARCH("搜索", Icons.Rounded.Search),
    DOWNLOADS("下载", Icons.Rounded.Download),
    LIBRARY("音乐库", Icons.Rounded.LibraryMusic),
    SETTINGS("设置", Icons.Rounded.Settings),
}

private val QUALITY_OPTIONS = listOf(
    128 to "128K",
    192 to "192K",
    320 to "320K",
    740 to "16-bit 无损",
    999 to "24-bit 无损",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GdMusicApp(
    viewModel: MainViewModel,
    themeSettings: ThemeSettings,
    onThemeSettingsChange: (ThemeSettings) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val library by viewModel.library.collectAsStateWithLifecycle()
    var destinationIndex by rememberSaveable { mutableIntStateOf(0) }
    var showPlayer by rememberSaveable { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showQualityPicker by remember { mutableStateOf(false) }
    var showSpeedPicker by remember { mutableStateOf(false) }
    var showSourceManager by remember { mutableStateOf(false) }
    var pendingDownload by remember { mutableStateOf<Track?>(null) }
    var showBatchDownload by remember { mutableStateOf(false) }
    var pendingPlaylistTrack by remember { mutableStateOf<Track?>(null) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val audioPermission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.scanLocalMusic()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text("GD Music", fontWeight = FontWeight.Bold)
                        Text(
                            Destination.entries[destinationIndex].label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    if (state.isResolving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 18.dp).size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                AnimatedVisibility(state.current != null) {
                    MiniPlayer(
                        state = state,
                        onOpen = { showPlayer = true },
                        onToggle = viewModel::togglePlayback,
                        onPrevious = viewModel::playPrevious,
                        onNext = viewModel::playNext,
                    )
                }
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    Destination.entries.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = destinationIndex == index,
                            onClick = { destinationIndex = index },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            ) {
                when (Destination.entries[destinationIndex]) {
                    Destination.HOME -> HomeScreen(
                        state = state,
                        library = library,
                        downloadCount = downloads.size,
                        onSearch = { destinationIndex = Destination.SEARCH.ordinal },
                        onPlay = viewModel::play,
                        onDownload = { pendingDownload = it },
                        onResume = viewModel::resumeLastSession,
                        onOpenLibrary = { destinationIndex = Destination.LIBRARY.ordinal },
                        onOpenDownloads = { destinationIndex = Destination.DOWNLOADS.ordinal },
                    )
                    Destination.SEARCH -> SearchScreen(
                        state = state,
                        onQueryChange = viewModel::updateQuery,
                        onSourceChange = viewModel::selectSource,
                        onSearch = viewModel::search,
                        onPlay = viewModel::play,
                        onDownload = { pendingDownload = it },
                        isLiked = { it.libraryKey in library.likedKeys },
                        onToggleLiked = viewModel::toggleLiked,
                        onPlayNext = viewModel::playNextInQueue,
                        onAddToQueue = viewModel::addToQueue,
                        onAddToPlaylist = { pendingPlaylistTrack = it },
                        onBatchDownload = { showBatchDownload = true },
                        onManageSources = { showSourceManager = true },
                    )
                    Destination.DOWNLOADS -> DownloadsScreen(
                        items = downloads,
                        onCancel = viewModel::cancelDownload,
                        onRetry = viewModel::retryDownload,
                        onRemove = viewModel::removeDownload,
                        onClearFinished = viewModel::clearFinishedDownloads,
                        onGoSearch = { destinationIndex = Destination.SEARCH.ordinal },
                    )
                    Destination.LIBRARY -> LibraryScreen(
                        state = state,
                        library = library,
                        onPlay = viewModel::play,
                        onToggleLiked = viewModel::toggleLiked,
                        onCreatePlaylist = { showCreatePlaylist = true },
                        onDeletePlaylist = viewModel::deletePlaylist,
                        onRenamePlaylist = viewModel::renamePlaylist,
                        onRemoveFromPlaylist = viewModel::removeFromPlaylist,
                        onRemoveFromQueue = viewModel::removeFromQueue,
                        onMoveQueueItem = viewModel::moveQueueItem,
                        onClearQueue = viewModel::clearQueue,
                        onClearHistory = viewModel::clearHistory,
                        onScanLocal = {
                            if (ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.scanLocalMusic()
                            } else audioPermissionLauncher.launch(audioPermission)
                        },
                    )
                    Destination.SETTINGS -> SettingsScreen(
                        state = state,
                        onBitrateChange = viewModel::setBitrate,
                        themeSettings = themeSettings,
                        onThemeSettingsChange = onThemeSettingsChange,
                    )
                }
            }
        }
    }

    if (showPlayer && state.current != null) {
        Dialog(
            onDismissRequest = { showPlayer = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
            ) {
            NowPlayingSheet(
                state = state,
                onToggle = viewModel::togglePlayback,
                onPrevious = viewModel::playPrevious,
                onNext = viewModel::playNext,
                onCyclePlaybackMode = viewModel::cyclePlaybackMode,
                onToggleShuffle = viewModel::toggleShuffle,
                onSeek = viewModel::seekTo,
                onDownload = { track ->
                    pendingDownload = track
                    showPlayer = false
                },
                isLiked = currentTrackLiked(library, state.current?.track),
                onToggleLiked = { state.current?.track?.let(viewModel::toggleLiked) },
                onAddToPlaylist = { state.current?.track?.let { pendingPlaylistTrack = it } },
                onRemoveFromQueue = viewModel::removeFromQueue,
                onPlayFromQueue = viewModel::play,
                onSleepTimer = { showSleepTimer = true },
                onChooseQuality = { showQualityPicker = true },
                onChooseSpeed = { showSpeedPicker = true },
                onDismiss = { showPlayer = false },
            )
            }
        }
    }

    if (showQualityPicker) {
        AlertDialog(
            onDismissRequest = { showQualityPicker = false },
            title = { Text("播放音质") },
            text = {
                Column {
                    QUALITY_OPTIONS.forEach { (value, label) ->
                        TextButton(
                            onClick = {
                                viewModel.setBitrate(value)
                                showQualityPicker = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (state.bitrate == value) "$label · 当前" else label)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showQualityPicker = false }) { Text("完成") } },
        )
    }

    if (showSpeedPicker) {
        AlertDialog(
            onDismissRequest = { showSpeedPicker = false },
            title = { Text("播放速度") },
            text = {
                Column {
                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                        TextButton(
                            onClick = { viewModel.setPlaybackSpeed(speed); showSpeedPicker = false },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(if (state.playbackSpeed == speed) "${speed}x · 当前" else "${speed}x") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSpeedPicker = false }) { Text("完成") } },
        )
    }

    if (showSleepTimer) {
        AlertDialog(
            onDismissRequest = { showSleepTimer = false },
            title = { Text("睡眠定时") },
            text = { Text("到时间后会暂停当前播放。") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(15, 30, 45, 60).forEach { minutes ->
                        TextButton(onClick = {
                            viewModel.setSleepTimer(minutes)
                            showSleepTimer = false
                        }) { Text("${minutes}分") }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setSleepTimer(null)
                    showSleepTimer = false
                }) { Text("取消定时") }
            },
        )
    }

    if (showSourceManager) {
        SourceManagerSheet(
            state = state,
            onMove = viewModel::moveSource,
            onVisibilityChange = viewModel::setSourceVisible,
            onDismiss = { showSourceManager = false },
        )
    }

    pendingDownload?.let { track ->
        DownloadOptionsSheet(
            track = track,
            options = state.downloadOptions,
            onBitrateChange = viewModel::setDownloadBitrate,
            onNameFormatChange = viewModel::setDownloadNameFormat,
            onEmbedChange = viewModel::setEmbedMetadata,
            onLyricChange = viewModel::setSaveLyricFile,
            onConfirm = {
                viewModel.download(track)
                pendingDownload = null
            },
            onDismiss = { pendingDownload = null },
        )
    }

    if (showBatchDownload) {
        BatchDownloadSheet(
            total = state.results.size,
            options = state.downloadOptions,
            onBitrateChange = viewModel::setDownloadBitrate,
            onNameFormatChange = viewModel::setDownloadNameFormat,
            onEmbedChange = viewModel::setEmbedMetadata,
            onLyricChange = viewModel::setSaveLyricFile,
            onConfirm = { range ->
                viewModel.downloadBatch(state.results, range)
                showBatchDownload = false
            },
            onDismiss = { showBatchDownload = false },
        )
    }

    pendingPlaylistTrack?.let { track ->
        AddToPlaylistSheet(
            track = track,
            playlists = library.playlists,
            onAdd = { playlistId ->
                viewModel.addToPlaylist(playlistId, track)
                pendingPlaylistTrack = null
            },
            onCreate = { showCreatePlaylist = true },
            onDismiss = { pendingPlaylistTrack = null },
        )
    }

    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onConfirm = { name ->
                val id = viewModel.createPlaylist(name)
                if (id != null) {
                    pendingPlaylistTrack?.let { viewModel.addToPlaylist(id, it) }
                    pendingPlaylistTrack = null
                    showCreatePlaylist = false
                }
                id != null
            },
            onDismiss = { showCreatePlaylist = false },
        )
    }
}

private fun currentTrackLiked(library: LibraryState, track: Track?): Boolean =
    track != null && track.libraryKey in library.likedKeys

@Composable
private fun HomeScreen(
    state: MusicUiState,
    library: LibraryState,
    downloadCount: Int,
    onSearch: () -> Unit,
    onPlay: (Track) -> Unit,
    onDownload: (Track) -> Unit,
    onResume: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        state.restorableTrack?.let { track ->
            item {
                Card(onClick = onResume, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("继续上次播放", fontWeight = FontWeight.SemiBold)
                            Text("${track.name} · ${track.artistText}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onSearch),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(28.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Icon(
                        Icons.Rounded.Headphones,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.height(28.dp))
                    Text("今天想听什么？", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("从 10 个公开音乐源搜索，轻点即可试听。")
                    Spacer(Modifier.height(18.dp))
                    AssistChip(
                        onClick = onSearch,
                        label = { Text("开始搜索") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Favorite,
                    title = "我喜欢",
                    subtitle = "${library.likedTracks.size} 首歌曲",
                    onClick = onOpenLibrary,
                )
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.LibraryMusic,
                    title = "本地音乐",
                    subtitle = "${state.localTracks.size} 首已扫描",
                    onClick = onOpenLibrary,
                )
            }
        }
        item {
            FeatureCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.Download,
                title = "下载任务",
                subtitle = if (downloadCount == 0) "暂无任务" else "$downloadCount 个任务",
                onClick = onOpenDownloads,
            )
        }
        if (state.queue.isNotEmpty()) {
            item {
                Text("最近播放", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            items(state.queue.takeLast(5).reversed(), key = { "home-${it.source}-${it.id}" }) { track ->
                CompactTrackRow(track = track, onClick = { onPlay(track) }, onDownload = { onDownload(track) })
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("尊重版权", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "本应用只调用公开接口的搜索、在线试听与下载能力。下载的音乐仅供个人收听，请支持正版音乐。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(20.dp))
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SearchScreen(
    state: MusicUiState,
    onQueryChange: (String) -> Unit,
    onSourceChange: (MusicSource) -> Unit,
    onSearch: () -> Unit,
    onPlay: (Track) -> Unit,
    onDownload: (Track) -> Unit,
    isLiked: (Track) -> Boolean,
    onToggleLiked: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onBatchDownload: () -> Unit,
    onManageSources: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.query,
                onValueChange = onQueryChange,
                singleLine = true,
                label = { Text("歌曲、歌手或专辑") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    FilledIconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onSearch()
                        },
                        enabled = state.query.isNotBlank() && !state.isSearching,
                    ) {
                        if (state.isSearching) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.Search, contentDescription = "搜索")
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("搜索音源", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onManageSources) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("管理")
                }
            }
            LazyRow(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.sourceOrder.filter { it !in state.hiddenSources }, key = { it.id }) { source ->
                    FilterChip(
                        selected = source == state.selectedSource,
                        onClick = { onSourceChange(source) },
                        label = { Text(source.displayName) },
                    )
                }
            }
        }
        when {
            state.results.isEmpty() && state.isSearching -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.results.isEmpty() -> EmptyState(
                icon = Icons.Rounded.Search,
                title = "搜索全网曲库",
                subtitle = "选择音乐源并输入关键词，结果会显示音质与来源。",
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${state.selectedSource.displayName} · ${state.results.size} 首结果",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onBatchDownload) {
                            Icon(Icons.Rounded.Downloading, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("批量下载")
                        }
                    }
                }
                items(state.results, key = { "${it.source}-${it.id}" }) { track ->
                    TrackRow(
                        track = track,
                        onPlay = { onPlay(track) },
                        onDownload = { onDownload(track) },
                        liked = isLiked(track),
                        onToggleLiked = { onToggleLiked(track) },
                        onPlayNext = { onPlayNext(track) },
                        onAddToQueue = { onAddToQueue(track) },
                        onAddToPlaylist = { onAddToPlaylist(track) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    liked: Boolean,
    onToggleLiked: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    ListItem(
        modifier = Modifier.clickable(onClick = onPlay),
        leadingContent = {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null)
                }
            }
        },
        headlineContent = { Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column {
                Text(track.artistText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(track.album, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                    if (track.hasHiRes) {
                        Text("Hi-Res", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onToggleLiked) {
                    Icon(
                        if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (liked) "取消喜欢 ${track.name}" else "喜欢 ${track.name}",
                        tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "更多操作 ${track.name}")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("下一首播放") }, onClick = { menuExpanded = false; onPlayNext() })
                        DropdownMenuItem(text = { Text("加入队列") }, onClick = { menuExpanded = false; onAddToQueue() })
                        DropdownMenuItem(text = { Text("添加到歌单") }, onClick = { menuExpanded = false; onAddToPlaylist() })
                        DropdownMenuItem(text = { Text("下载") }, onClick = { menuExpanded = false; onDownload() })
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourceManagerSheet(
    state: MusicUiState,
    onMove: (MusicSource, Int) -> Unit,
    onVisibilityChange: (MusicSource, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.82f).padding(horizontal = 20.dp),
        ) {
            Text("管理音源", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "从上到下也是播放解析失败时的自动回退顺序。服务状态来自 GD 音乐台当前 API 公告。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(state.sourceOrder, key = { _, source -> source.id }) { index, source ->
                    val visible = source !in state.hiddenSources
                    ListItem(
                        leadingContent = {
                            Switch(
                                checked = visible,
                                onCheckedChange = { onVisibilityChange(source, it) },
                                modifier = Modifier.semantics {
                                    contentDescription = if (visible) "隐藏 ${source.displayName}" else "显示 ${source.displayName}"
                                },
                            )
                        },
                        headlineContent = { Text(source.displayName, fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Text(
                                source.availability.label,
                                color = when (source.availability) {
                                    SourceAvailability.STABLE -> MaterialTheme.colorScheme.primary
                                    SourceAvailability.UNAVAILABLE -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { onMove(source, -1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "上移 ${source.displayName}")
                                }
                                IconButton(
                                    onClick = { onMove(source, 1) },
                                    enabled = index < state.sourceOrder.lastIndex,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "下移 ${source.displayName}")
                                }
                            }
                        },
                    )
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("完成")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CompactTrackRow(track: Track, onClick: () -> Unit, onDownload: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(Modifier.size(44.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.MusicNote, contentDescription = null) }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artistText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDownload) {
            Icon(Icons.Rounded.Download, contentDescription = "下载 ${track.name}")
        }
    }
}

@Composable
private fun LibraryScreen(
    state: MusicUiState,
    library: LibraryState,
    onPlay: (Track) -> Unit,
    onToggleLiked: (Track) -> Unit,
    onCreatePlaylist: () -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onRenamePlaylist: (String, String) -> Boolean,
    onRemoveFromPlaylist: (String, Track) -> Unit,
    onRemoveFromQueue: (Track) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onClearQueue: () -> Unit,
    onClearHistory: () -> Unit,
    onScanLocal: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedPlaylistId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedPlaylist = library.playlists.firstOrNull { it.id == selectedPlaylistId }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            listOf("喜欢", "歌单", "本地", "历史", "队列").forEachIndexed { index, label ->
                Tab(selected = selectedTab == index, onClick = {
                    selectedTab = index
                    if (index != 1) selectedPlaylistId = null
                }, text = { Text(label) })
            }
        }
        when (selectedTab) {
            0 -> LibraryTrackList(
                tracks = library.likedTracks,
                emptyTitle = "还没有喜欢的音乐",
                emptySubtitle = "在搜索结果或播放页点按心形按钮收藏。",
                onPlay = onPlay,
                trailing = { track ->
                    IconButton(onClick = { onToggleLiked(track) }) {
                        Icon(Icons.Rounded.Favorite, contentDescription = "取消喜欢 ${track.name}", tint = MaterialTheme.colorScheme.primary)
                    }
                },
            )
            1 -> if (selectedPlaylist == null) {
                PlaylistList(
                    playlists = library.playlists,
                    onOpen = { selectedPlaylistId = it.id },
                    onCreate = onCreatePlaylist,
                    onDelete = onDeletePlaylist,
                    onRename = onRenamePlaylist,
                )
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { selectedPlaylistId = null }) { Text("返回") }
                        Text(selectedPlaylist.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    LibraryTrackList(
                        tracks = selectedPlaylist.tracks,
                        emptyTitle = "歌单还是空的",
                        emptySubtitle = "从搜索结果或播放页把歌曲加入这里。",
                        onPlay = onPlay,
                        trailing = { track ->
                            IconButton(onClick = { onRemoveFromPlaylist(selectedPlaylist.id, track) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "从歌单移除 ${track.name}")
                            }
                        },
                    )
                }
            }
            2 -> Column(Modifier.fillMaxSize()) {
                FilledTonalButton(onClick = onScanLocal, modifier = Modifier.align(Alignment.End).padding(12.dp)) { Text("扫描本地音乐") }
                LibraryTrackList(
                    tracks = state.localTracks,
                    emptyTitle = "还没有本地音乐",
                    emptySubtitle = "授权读取音频后，从系统媒体库扫描歌曲。",
                    onPlay = onPlay,
                    trailing = {},
                )
            }
            3 -> Column(Modifier.fillMaxSize()) {
                if (library.historyTracks.isNotEmpty()) {
                    TextButton(onClick = onClearHistory, modifier = Modifier.align(Alignment.End).padding(end = 12.dp)) { Text("清空历史") }
                }
                LibraryTrackList(
                    tracks = library.historyTracks,
                    emptyTitle = "暂无播放历史",
                    emptySubtitle = "成功开始播放的歌曲会记录在这里。",
                    onPlay = onPlay,
                    trailing = {},
                )
            }
            else -> QueueScreen(state, onPlay, onRemoveFromQueue, onMoveQueueItem, onClearQueue)
        }
    }
}

@Composable
private fun LibraryTrackList(
    tracks: List<Track>,
    emptyTitle: String,
    emptySubtitle: String,
    onPlay: (Track) -> Unit,
    trailing: @Composable (Track) -> Unit,
) {
    if (tracks.isEmpty()) {
        EmptyState(Icons.Rounded.LibraryMusic, emptyTitle, emptySubtitle)
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
        item { Text("${tracks.size} 首歌曲", Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium) }
        items(tracks, key = { "library-${it.libraryKey}" }) { track ->
            ListItem(
                modifier = Modifier.clip(RoundedCornerShape(18.dp)).clickable { onPlay(track) },
                leadingContent = { Icon(Icons.Rounded.MusicNote, contentDescription = null) },
                headlineContent = { Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text(track.artistText, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                trailingContent = { trailing(track) },
            )
        }
    }
}

@Composable
private fun PlaylistList(
    playlists: List<LocalPlaylist>,
    onOpen: (LocalPlaylist) -> Unit,
    onCreate: () -> Unit,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Boolean,
) {
    var pendingDelete by remember { mutableStateOf<LocalPlaylist?>(null) }
    var pendingRename by remember { mutableStateOf<LocalPlaylist?>(null) }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("我的歌单", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FilledTonalButton(onClick = onCreate) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("新建")
            }
        }
        if (playlists.isEmpty()) {
            EmptyState(Icons.Rounded.LibraryMusic, "还没有自建歌单", "新建歌单后，可从搜索结果或播放页添加歌曲。")
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                items(playlists, key = { it.id }) { playlist ->
                    ListItem(
                        modifier = Modifier.clip(RoundedCornerShape(18.dp)).clickable { onOpen(playlist) },
                        leadingContent = { Icon(Icons.Rounded.LibraryMusic, contentDescription = null) },
                        headlineContent = { Text(playlist.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("${playlist.tracks.size} 首歌曲") },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { pendingRename = playlist }) { Text("改名") }
                                IconButton(onClick = { pendingDelete = playlist }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "删除歌单 ${playlist.name}")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
    pendingDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除歌单？") },
            text = { Text("将删除“${playlist.name}”及其本地歌单记录，不会删除已下载文件。") },
            confirmButton = {
                TextButton(onClick = { onDelete(playlist.id); pendingDelete = null }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("取消") } },
        )
    }
    pendingRename?.let { playlist ->
        var name by remember(playlist.id) { mutableStateOf(playlist.name) }
        var hasError by remember(playlist.id) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { pendingRename = null },
            title = { Text("歌单改名") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; hasError = false },
                    label = { Text("歌单名称") },
                    singleLine = true,
                    isError = hasError,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (onRename(playlist.id, name)) pendingRename = null else hasError = true
                }, enabled = name.isNotBlank()) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { pendingRename = null }) { Text("取消") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistSheet(
    track: Track,
    playlists: List<LocalPlaylist>,
    onAdd: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding()) {
            Text("添加到歌单", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(track.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("新建歌单")
            }
            playlists.forEach { playlist ->
                ListItem(
                    modifier = Modifier.clip(RoundedCornerShape(18.dp)).clickable { onAdd(playlist.id) },
                    leadingContent = { Icon(Icons.Rounded.LibraryMusic, contentDescription = null) },
                    headlineContent = { Text(playlist.name) },
                    supportingContent = { Text("${playlist.tracks.size} 首歌曲") },
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CreatePlaylistDialog(onConfirm: (String) -> Boolean, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建歌单") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; hasError = false },
                label = { Text("歌单名称") },
                singleLine = true,
                isError = hasError,
                supportingText = if (hasError) ({ Text("名称不能为空，也不能与现有歌单重复") }) else null,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (!onConfirm(name)) hasError = true }, enabled = name.isNotBlank()) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun QueueScreen(
    state: MusicUiState,
    onPlay: (Track) -> Unit,
    onRemove: (Track) -> Unit,
    onMove: (Int, Int) -> Unit = { _, _ -> },
    onClear: () -> Unit = {},
) {
    if (state.queue.isEmpty()) {
        EmptyState(Icons.AutoMirrored.Rounded.QueueMusic, "播放队列为空", "从搜索结果中添加歌曲，它们会出现在这里。")
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${state.queue.size} 首歌曲", modifier = Modifier.padding(8.dp).weight(1f), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClear) { Text("清空") }
            }
        }
        itemsIndexed(state.queue, key = { _, it -> "queue-${it.source}-${it.id}" }) { index, track ->
            val isCurrent = state.current?.track == track
            ListItem(
                modifier = Modifier.clip(RoundedCornerShape(18.dp)).clickable { onPlay(track) },
                colors = ListItemDefaults.colors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.background,
                ),
                leadingContent = {
                    Icon(
                        if (isCurrent && state.isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                headlineContent = { Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text(track.artistText, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onMove(index, -1) }, enabled = index > 0) {
                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "上移 ${track.name}")
                        }
                        IconButton(onClick = { onMove(index, 1) }, enabled = index < state.queue.lastIndex) {
                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "下移 ${track.name}")
                        }
                        IconButton(onClick = { onRemove(track) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "从队列移除 ${track.name}")
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadsScreen(
    items: List<DownloadItem>,
    onCancel: (String) -> Unit,
    onRetry: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClearFinished: () -> Unit,
    onGoSearch: () -> Unit,
) {
    val context = LocalContext.current
    if (items.isEmpty()) {
        EmptyState(
            icon = Icons.Rounded.Download,
            title = "还没有下载任务",
            subtitle = "在搜索结果或播放页点击下载图标，文件会保存到 音乐/$DOWNLOAD_FOLDER。",
        )
        return
    }
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${items.count { it.isActive }} 个进行中 · 共 ${items.size} 个",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (items.any { it.isFinished }) {
                    TextButton(onClick = onClearFinished) { Text("清除已完成") }
                }
            }
        }
        items(items, key = { it.id }) { item ->
            DownloadItemRow(
                item = item,
                onCancel = { onCancel(item.id) },
                onRetry = { onRetry(item.id) },
                onRemove = { onRemove(item.id) },
                onOpen = {
                    val uri = item.uri ?: return@DownloadItemRow
                    val extension = item.fileName?.substringAfterLast('.', "").orEmpty()
                    val intent = Intent(Intent.ACTION_VIEW)
                        .setDataAndType(uri, mimeTypeOf(extension))
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    runCatching { context.startActivity(intent) }
                        .onFailure { Toast.makeText(context, "没有可以打开该文件的播放器", Toast.LENGTH_SHORT).show() }
                },
            )
        }
        item {
            TextButton(modifier = Modifier.padding(16.dp), onClick = onGoSearch) {
                Text("继续搜索音乐")
            }
        }
    }
}

@Composable
private fun DownloadItemRow(
    item: DownloadItem,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onRemove: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (item.status) {
                        DownloadStatus.COMPLETED -> Icons.Rounded.CheckCircle
                        DownloadStatus.FAILED -> Icons.Rounded.Close
                        DownloadStatus.CANCELED -> Icons.Rounded.Close
                        else -> Icons.Rounded.Downloading
                    },
                    contentDescription = null,
                    tint = when (item.status) {
                        DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.track.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Text(
                        item.track.artistText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                when {
                    item.isActive -> IconButton(onClick = onCancel) {
                        Icon(Icons.Rounded.Close, contentDescription = "取消下载 ${item.track.name}")
                    }
                    item.status == DownloadStatus.COMPLETED -> IconButton(onClick = onOpen) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "打开 ${item.track.name}")
                    }
                    else -> IconButton(onClick = onRetry) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "重新下载 ${item.track.name}")
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.Delete, contentDescription = "从列表移除 ${item.track.name}")
                }
            }
            Spacer(Modifier.height(10.dp))
            if (item.isActive) {
                if (item.status == DownloadStatus.DOWNLOADING && item.totalBytes > 0L) {
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.statusText(),
                style = MaterialTheme.typography.bodySmall,
                color = when (item.status) {
                    DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

private fun DownloadItem.statusText(): String = when (status) {
    DownloadStatus.QUEUED -> "等待中 · ${bitrateLabel(bitrate)}"
    DownloadStatus.RESOLVING -> "正在解析下载地址 · ${bitrateLabel(bitrate)}"
    DownloadStatus.DOWNLOADING -> buildString {
        append("下载中 ${(progress * 100).toInt()}%")
        if (totalBytes > 0L) append(" · ${formatBytes(receivedBytes)} / ${formatBytes(totalBytes)}")
        append(" · ${bitrateLabel(bitrate)}")
    }
    DownloadStatus.FINALIZING -> "写入文件与标签 · ${bitrateLabel(bitrate)}"
    DownloadStatus.COMPLETED -> buildString {
        append("已完成")
        fileName?.let { append(" · $it") }
        if (receivedBytes > 0L) append(" · ${formatBytes(receivedBytes)}")
    }
    DownloadStatus.FAILED -> message?.let { "下载失败：$it" } ?: "下载失败"
    DownloadStatus.CANCELED -> "已取消"
}

private fun bitrateLabel(bitrate: Int): String = when {
    bitrate > 900 -> "24-bit 无损"
    bitrate > 700 -> "16-bit 无损"
    else -> "${bitrate}K"
}

private fun formatBytes(value: Long): String {
    if (value <= 0L) return "0 B"
    val mb = value / 1_048_576.0
    return if (mb >= 1.0) "%.1f MB".format(mb) else "%.0f KB".format(value / 1024.0)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DownloadOptionsSheet(
    track: Track,
    options: DownloadOptions,
    onBitrateChange: (Int) -> Unit,
    onNameFormatChange: (NameFormat) -> Unit,
    onEmbedChange: (Boolean) -> Unit,
    onLyricChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text("下载歌曲", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "${track.name} · ${track.artistText}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            DownloadOptionBody(
                options = options,
                previewTrack = track,
                onBitrateChange = onBitrateChange,
                onNameFormatChange = onNameFormatChange,
                onEmbedChange = onEmbedChange,
                onLyricChange = onLyricChange,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("开始下载")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BatchDownloadSheet(
    total: Int,
    options: DownloadOptions,
    onBitrateChange: (Int) -> Unit,
    onNameFormatChange: (NameFormat) -> Unit,
    onEmbedChange: (Boolean) -> Unit,
    onLyricChange: (Boolean) -> Unit,
    onConfirm: (IntRange) -> Unit,
    onDismiss: () -> Unit,
) {
    val maxCount = total.coerceAtMost(MAX_BATCH_SIZE)
    var start by rememberSaveable { mutableStateOf("1") }
    var end by rememberSaveable { mutableStateOf(maxCount.toString()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text("批量下载", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "当前结果 $total 首，单次最多加入 $MAX_BATCH_SIZE 首，与官网批量下载一致。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = start,
                    onValueChange = { start = it.filter(Char::isDigit).take(4) },
                    singleLine = true,
                    label = { Text("起始") },
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = end,
                    onValueChange = { end = it.filter(Char::isDigit).take(4) },
                    singleLine = true,
                    label = { Text("结束") },
                )
            }
            Spacer(Modifier.height(18.dp))
            DownloadOptionBody(
                options = options,
                previewTrack = null,
                onBitrateChange = onBitrateChange,
                onNameFormatChange = onNameFormatChange,
                onEmbedChange = onEmbedChange,
                onLyricChange = onLyricChange,
            )
            Spacer(Modifier.height(20.dp))
            val from = (start.toIntOrNull() ?: 1).coerceIn(1, maxCount)
            val to = (end.toIntOrNull() ?: maxCount).coerceIn(from, maxCount)
            Button(
                onClick = { onConfirm((from - 1) until to) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Icon(Icons.Rounded.Downloading, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("下载第 $from - $to 首")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DownloadOptionBody(
    options: DownloadOptions,
    previewTrack: Track?,
    onBitrateChange: (Int) -> Unit,
    onNameFormatChange: (NameFormat) -> Unit,
    onEmbedChange: (Boolean) -> Unit,
    onLyricChange: (Boolean) -> Unit,
) {
    Text("下载音质", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QUALITY_OPTIONS.forEach { (value, label) ->
            FilterChip(
                selected = options.bitrate == value,
                onClick = { onBitrateChange(value) },
                label = { Text(label) },
            )
        }
    }
    Spacer(Modifier.height(18.dp))
    Text("文件命名", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NameFormat.entries.forEach { format ->
            FilterChip(
                selected = options.nameFormat == format,
                onClick = { onNameFormatChange(format) },
                label = { Text(format.previewLabel(previewTrack)) },
            )
        }
    }
    Spacer(Modifier.height(18.dp))
    Text("附加内容", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(4.dp))
    SwitchRow(
        title = "写入歌曲信息",
        subtitle = "MP3 内嵌标题、歌手、专辑、封面与歌词",
        checked = options.embedMetadata,
        onCheckedChange = onEmbedChange,
    )
    SwitchRow(
        title = "同时保存歌词文件",
        subtitle = "在音乐目录生成同名 .lrc 文件",
        checked = options.saveLyricFile,
        onCheckedChange = onLyricChange,
    )
}

private fun NameFormat.previewLabel(track: Track?): String {
    if (track == null) return displayName
    val preview = when (this) {
        NameFormat.ARTIST_TITLE -> "${track.artistText} - ${track.name}"
        NameFormat.TITLE_ARTIST -> "${track.name} - ${track.artistText}"
        NameFormat.TITLE_ONLY -> track.name
    }
    return "$displayName（$preview）"
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}

@Composable
private fun SettingsScreen(
    state: MusicUiState,
    onBitrateChange: (Int) -> Unit,
    themeSettings: ThemeSettings,
    onThemeSettingsChange: (ThemeSettings) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text("外观与主题", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Android 12 及以上可使用壁纸生成的莫奈动态配色。关闭后使用所选种子色。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeSettings.mode == mode,
                        onClick = { onThemeSettingsChange(themeSettings.copy(mode = mode)) },
                        label = { Text(mode.label) },
                    )
                }
            }
        }
        item {
            SwitchRow(
                title = "莫奈动态取色",
                subtitle = if (Build.VERSION.SDK_INT >= 31) "跟随系统壁纸配色" else "需要 Android 12 或更高版本",
                checked = themeSettings.monetEnabled && Build.VERSION.SDK_INT >= 31,
                onCheckedChange = { onThemeSettingsChange(themeSettings.copy(monetEnabled = it)) },
            )
        }
        item {
            Text("种子色", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SeedColor.entries.forEach { seed ->
                    FilterChip(
                        selected = themeSettings.seedColor == seed,
                        onClick = {
                            onThemeSettingsChange(
                                themeSettings.copy(seedColor = seed, monetEnabled = false),
                            )
                        },
                        label = { Text(seed.label) },
                    )
                }
            }
        }
        item { HorizontalDivider() }
        item {
            Text("播放音质", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "音乐源不支持所选音质时，服务会返回最接近的可用版本。下载音质在下载面板中单独设置。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(QUALITY_OPTIONS) { quality ->
            FilterChip(
                modifier = Modifier.fillMaxWidth(),
                selected = state.bitrate == quality.first,
                onClick = { onBitrateChange(quality.first) },
                label = { Text(quality.second, modifier = Modifier.padding(vertical = 8.dp)) },
                leadingIcon = { Icon(Icons.Rounded.HighQuality, contentDescription = null) },
            )
        }
        item { HorizontalDivider() }
        item {
            Text("下载说明", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "下载的音乐保存到公共目录 音乐/$DOWNLOAD_FOLDER，文件按所选格式自动命名；" +
                    "开启写入歌曲信息后，MP3 会内嵌封面与歌词。Android 10 及以上通过媒体库保存，无需存储权限。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { HorizontalDivider() }
        item {
            Text("关于数据源", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "GD Music 使用 music-api.gdstudio.xyz 的公开 API 进行搜索、在线试听与下载。当前稳定源以服务公告为准，接口限制为 5 分钟不超过 50 次请求。资源与版权归对应平台及权利人所有。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.LibraryMusic, contentDescription = null)
                    Text("版本 2.7.1 · targetSdk 36")
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize().padding(36.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(24.dp).size(42.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MiniPlayer(
    state: MusicUiState,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val current = state.current ?: return
    var horizontalDrag by remember(current.track.id) { mutableStateOf(0f) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(current.track.id) {
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDrag = 0f },
                    onHorizontalDrag = { _, delta -> horizontalDrag += delta },
                    onDragEnd = {
                        when {
                            horizontalDrag > 72f -> onPrevious()
                            horizontalDrag < -72f -> onNext()
                        }
                    },
                )
            }
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { contentDescription = "正在播放 ${current.track.name}" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(current.artworkUrl, Modifier.size(48.dp), RoundedCornerShape(14.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(current.track.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(current.track.artistText, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onToggle) {
            Icon(
                if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (state.isPlaying) "暂停" else "播放",
            )
        }
    }
}

@Composable
private fun NowPlayingSheet(
    state: MusicUiState,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCyclePlaybackMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSeek: (Long) -> Unit,
    onDownload: (Track) -> Unit,
    isLiked: Boolean,
    onToggleLiked: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveFromQueue: (Track) -> Unit,
    onPlayFromQueue: (Track) -> Unit,
    onSleepTimer: () -> Unit,
    onChooseQuality: () -> Unit,
    onChooseSpeed: () -> Unit,
    onDismiss: () -> Unit,
) {
    val current = state.current ?: return
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var headerDragDistance by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { headerDragDistance = 0f },
                        onVerticalDrag = { _, delta -> headerDragDistance += delta },
                        onDragEnd = { if (headerDragDistance > 96f) onDismiss() },
                    )
                }
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "关闭播放页")
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(current.track.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(current.track.artistText, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onToggleLiked, modifier = Modifier.size(48.dp)) {
                Icon(
                    if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isLiked) "取消喜欢" else "喜欢",
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            listOf("封面" to Icons.Rounded.Album, "歌词" to Icons.Rounded.Lyrics, "队列" to Icons.AutoMirrored.Rounded.QueueMusic).forEachIndexed { index, item ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(item.first) },
                    icon = { Icon(item.second, contentDescription = item.first) },
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (selectedTab) {
                0 -> PlayerCoverTab(
                    state = state,
                    onToggle = onToggle,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onCyclePlaybackMode = onCyclePlaybackMode,
                    onToggleShuffle = onToggleShuffle,
                    onChooseSpeed = onChooseSpeed,
                    onSeek = onSeek,
                    onShowLyrics = { selectedTab = 1 },
                )
                1 -> LyricsTab(state.lyricLines, state.positionMs, onSeek)
                else -> PlayerQueueTab(state, onPlayFromQueue, onRemoveFromQueue)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            PlayerShortcut(Icons.Rounded.Download, "下载", { onDownload(current.track) })
            PlayerShortcut(Icons.AutoMirrored.Rounded.PlaylistAdd, "歌单", onAddToPlaylist)
            PlayerShortcut(Icons.Rounded.HighQuality, "音质", onChooseQuality)
            PlayerShortcut(Icons.Rounded.Timer, "定时", onSleepTimer)
        }
    }
}

@Composable
private fun PlayerCoverTab(
    state: MusicUiState,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCyclePlaybackMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onChooseSpeed: () -> Unit,
    onSeek: (Long) -> Unit,
    onShowLyrics: () -> Unit,
) {
    val current = state.current ?: return
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(20.dp))
        AlbumArt(
            current.artworkUrl,
            Modifier.fillMaxWidth().height(280.dp).clickable(onClick = onShowLyrics),
            RoundedCornerShape(28.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(current.track.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(current.track.artistText, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Slider(
            value = state.positionMs.toFloat().coerceIn(0f, state.durationMs.coerceAtLeast(1L).toFloat()),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..state.durationMs.coerceAtLeast(1L).toFloat(),
            modifier = Modifier.semantics { contentDescription = "播放进度" },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(state.positionMs), style = MaterialTheme.typography.labelMedium)
            Text(formatTime(state.durationMs), style = MaterialTheme.typography.labelMedium)
        }
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleShuffle, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Rounded.Shuffle,
                    contentDescription = if (state.shuffleEnabled) "关闭随机播放" else "开启随机播放",
                    tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "上一首", modifier = Modifier.size(34.dp))
            }
            FilledIconButton(onClick = onToggle, modifier = Modifier.size(72.dp)) {
                Icon(if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (state.isPlaying) "暂停" else "播放", modifier = Modifier.size(38.dp))
            }
            IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "下一首", modifier = Modifier.size(34.dp))
            }
            IconButton(onClick = onCyclePlaybackMode, modifier = Modifier.size(48.dp)) {
                Icon(
                    if (state.playbackMode == PlaybackMode.REPEAT_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    contentDescription = state.playbackMode.label,
                    tint = if (state.playbackMode == PlaybackMode.SEQUENTIAL) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                )
            }
        }
        AssistChip(
            onClick = onChooseSpeed,
            label = { Text("${state.playbackSpeed}x") },
            leadingIcon = { Icon(Icons.Rounded.HighQuality, contentDescription = null, modifier = Modifier.size(18.dp)) },
        )
    }
}

@Composable
private fun PlayerShortcut(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(icon, contentDescription = label)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun LyricsTab(lines: List<LyricLine>, positionMs: Long, onSeek: (Long) -> Unit) {
    if (lines.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无同步歌词", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val activeIndex = lines.indexOfLast { it.timestampMs <= positionMs }
    val listState = rememberLazyListState()
    var pauseUntilMs by remember { mutableStateOf(0L) }
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) pauseUntilMs = android.os.SystemClock.uptimeMillis() + 3_000L
    }
    LaunchedEffect(activeIndex, pauseUntilMs) {
        if (activeIndex < 0) return@LaunchedEffect
        val waitMs = pauseUntilMs - android.os.SystemClock.uptimeMillis()
        if (waitMs > 0) kotlinx.coroutines.delay(waitMs)
        if (!listState.isScrollInProgress) listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(lines, key = { it.timestampMs }) { line ->
            val active = line == lines.getOrNull(activeIndex)
            Text(
                line.text,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                style = if (active) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable { onSeek(line.timestampMs) },
            )
        }
    }
}

@Composable
private fun PlayerQueueTab(state: MusicUiState, onPlay: (Track) -> Unit, onRemove: (Track) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 12.dp)) {
        items(state.queue, key = { "player-${it.source}-${it.id}" }) { track ->
            val active = track == state.current?.track
            ListItem(
                modifier = Modifier.clickable { onPlay(track) },
                colors = ListItemDefaults.colors(containerColor = if (active) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface),
                leadingContent = { Icon(if (active) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote, contentDescription = null) },
                headlineContent = { Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal) },
                supportingContent = { Text(track.artistText, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                trailingContent = {
                    IconButton(onClick = { onRemove(track) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "从队列删除 ${track.name}")
                    }
                },
            )
        }
    }
}

@Composable
private fun AlbumArt(url: String?, modifier: Modifier, shape: RoundedCornerShape) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.primaryContainer),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNullOrBlank()) {
            Icon(Icons.Rounded.Album, contentDescription = null, modifier = Modifier.size(52.dp))
        } else {
            AsyncImage(
                model = url,
                contentDescription = "专辑封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun formatTime(valueMs: Long): String {
    val totalSeconds = (valueMs / 1_000L).coerceAtLeast(0L)
    return "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

private const val MAX_BATCH_SIZE = 20
