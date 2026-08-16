package com.hdownloader.feature.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdownloader.core.common.DispatchersProvider
import com.hdownloader.core.download.model.DownloadState
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.download.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DownloadsTab(val title: String) {
    ALL("All"),
    DOWNLOADING("Downloading"),
    QUEUED("Queued"),
    COMPLETED("Completed"),
    FAILED("Failed"),
}

data class DownloadsUiState(
    val selectedTab: DownloadsTab = DownloadsTab.ALL,
    val all: List<DownloadState> = emptyList(),
    val downloading: List<DownloadState> = emptyList(),
    val queued: List<DownloadState> = emptyList(),
    val completed: List<DownloadState> = emptyList(),
    val failed: List<DownloadState> = emptyList(),
    val isMultiSelectMode: Boolean = false,
) {
    val selectedTabItems: List<DownloadState>
        get() = when (selectedTab) {
            DownloadsTab.ALL -> all
            DownloadsTab.DOWNLOADING -> downloading
            DownloadsTab.QUEUED -> queued
            DownloadsTab.COMPLETED -> completed
            DownloadsTab.FAILED -> failed
        }
}

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val dispatchers: DispatchersProvider,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(DownloadsTab.ALL)
    private val multiSelectMode = MutableStateFlow(false)

    val uiState: StateFlow<DownloadsUiState> =
        combine(
            downloadRepository.observeAll(),
            selectedTab,
            multiSelectMode,
        ) { downloads, tab, multiSelect ->
                DownloadsUiState(
                    selectedTab = tab,
                    all = downloads,
                    downloading = downloads.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PREPARING },
                    queued = downloads.filter { it.status == DownloadStatus.QUEUED || it.status == DownloadStatus.PAUSED },
                    completed = downloads.filter { it.status == DownloadStatus.COMPLETED },
                    failed = downloads.filter { it.status == DownloadStatus.FAILED },
                    isMultiSelectMode = multiSelect,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DownloadsUiState(),
            )

    fun selectTab(tab: DownloadsTab) {
        selectedTab.value = tab
    }

    fun setMultiSelectMode(enabled: Boolean) {
        multiSelectMode.value = enabled
    }

    fun pause(download: DownloadState) {
        viewModelScope.launch(dispatchers.io) {
            downloadRepository.pause(download.id)
        }
    }

    fun resume(download: DownloadState) {
        viewModelScope.launch(dispatchers.io) {
            downloadRepository.resume(download.id)
        }
    }

    fun cancel(download: DownloadState) {
        viewModelScope.launch(dispatchers.io) {
            downloadRepository.cancel(download.id)
        }
    }

    fun retry(download: DownloadState) {
        viewModelScope.launch(dispatchers.io) {
            downloadRepository.retry(download.id)
        }
    }

    fun remove(download: DownloadState) {
        viewModelScope.launch(dispatchers.io) {
            downloadRepository.remove(download.id)
        }
    }
}
