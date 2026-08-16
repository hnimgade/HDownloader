package com.hdownloader.feature.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdownloader.core.bookmark.repository.BookmarkRepository
import com.hdownloader.core.browser.repository.BrowserHistoryRepository
import com.hdownloader.core.common.DispatchersProvider
import com.hdownloader.core.download.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowserUiState(
    val urlInput: String = "",
    val currentUrl: String = "",
    val title: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isBookmarked: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val historyRepository: BrowserHistoryRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val dispatchers: DispatchersProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _downloadsQueued = MutableStateFlow(0)
    val downloadsQueued: StateFlow<Int> = _downloadsQueued.asStateFlow()

    fun onUrlChange(value: String) {
        _uiState.update { it.copy(urlInput = value) }
    }

    fun onPageStarted(url: String) {
        _uiState.update {
            it.copy(currentUrl = url, title = "", isLoading = true, progress = 10)
        }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.update { it.copy(progress = progress) }
    }

    fun onPageFinished(url: String, title: String?) {
        _uiState.update {
            it.copy(currentUrl = url, title = title.orEmpty(), isLoading = false, progress = 100)
        }
        viewModelScope.launch(dispatchers.io) {
            runCatching { historyRepository.record(url, title) }
        }
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                _uiState.update { it.copy(isBookmarked = bookmarkRepository.isBookmarked(url)) }
            }
        }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun navigate(url: String) {
        if (url.isBlank()) return
        _uiState.update { it.copy(urlInput = url) }
    }

    fun onDownloadDetected(
        url: String,
        contentDisposition: String?,
        mimeType: String?,
    ) {
        val fileName = BrowserUrlResolver.fileNameFor(url, contentDisposition, mimeType)
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                downloadRepository.addDownload(url = url, fileName = fileName)
            }.onSuccess {
                _downloadsQueued.update { it + 1 }
                _uiState.update { state ->
                    state.copy(message = "Download queued: $fileName")
                }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    fun toggleBookmark() {
        val url = _uiState.value.currentUrl
        if (url.isBlank()) return
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                if (bookmarkRepository.isBookmarked(url)) {
                    bookmarkRepository.observeAll().first()
                        .firstOrNull { it.url == url }
                        ?.let { bookmarkRepository.remove(it.id) }
                    _uiState.update { it.copy(isBookmarked = false) }
                } else {
                    bookmarkRepository.add(url, _uiState.value.title.takeIf { it.isNotBlank() })
                    _uiState.update { it.copy(isBookmarked = true) }
                }
            }
        }
    }
}
