package com.hdownloader.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdownloader.core.category.model.Category
import com.hdownloader.core.category.repository.CategoryRepository
import com.hdownloader.core.common.DispatchersProvider
import com.hdownloader.core.download.model.DownloadState
import com.hdownloader.core.download.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val downloads: List<DownloadState> = emptyList(),
    val activeDownloads: List<DownloadState> = emptyList(),
    val recentDownloads: List<DownloadState> = emptyList(),
    val totalSpeed: Long = 0L,
) {
    val activeCount: Int get() = activeDownloads.size
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val categoryRepository: CategoryRepository,
    private val dispatchers: DispatchersProvider,
) : ViewModel() {

    val categories: StateFlow<List<Category>> = categoryRepository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val uiState: StateFlow<HomeUiState> =
        downloadRepository.observeAll()
            .combine(MutableStateFlow(0L)) { downloads, _ ->
                val active = downloads.filter { it.status.isActive }
                HomeUiState(
                    downloads = downloads,
                    activeDownloads = active,
                    recentDownloads = downloads.take(5),
                    totalSpeed = active.sumOf { it.speed },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(),
            )

    fun addDownload(url: String, categoryId: Long?, startImmediately: Boolean = true) {
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                downloadRepository.addDownload(
                    url = url,
                    fileName = url.substringAfterLast('/').substringBefore('?'),
                    categoryId = categoryId,
                    startImmediately = startImmediately,
                )
            }
        }
    }

    fun pauseResume(download: DownloadState) {
        viewModelScope.launch(dispatchers.io) {
            if (download.status.isPausable) {
                downloadRepository.pause(download.id)
            } else if (download.status.isResumable) {
                downloadRepository.resume(download.id)
            }
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
}
