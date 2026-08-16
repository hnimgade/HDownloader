package com.hdownloader.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdownloader.core.settings.AppSettings
import com.hdownloader.core.settings.AppSettingsRepository
import com.hdownloader.core.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings(),
        )

    fun setThemeMode(mode: ThemeMode) = launch { settingsRepository.setThemeMode(mode) }

    fun setDynamicColor(enabled: Boolean) = launch { settingsRepository.setDynamicColor(enabled) }

    fun setMaxConcurrentDownloads(value: Int) = launch {
        settingsRepository.setMaxConcurrentDownloads(value.coerceIn(1, 10))
    }

    fun setMaxConnections(value: Int) = launch {
        settingsRepository.setMaxConnectionsPerDownload(value.coerceIn(1, 16))
    }

    fun setAutoRetry(enabled: Boolean) = launch { settingsRepository.setAutoRetry(enabled) }

    fun setWifiOnly(enabled: Boolean) = launch { settingsRepository.setWifiOnly(enabled) }

    fun setAutoStart(enabled: Boolean) = launch { settingsRepository.setAutoStartDownloads(enabled) }

    fun setClipboardDetection(enabled: Boolean) = launch {
        settingsRepository.setClipboardDetectionEnabled(enabled)
    }

    fun setBrowserMediaDetection(enabled: Boolean) = launch {
        settingsRepository.setBrowserMediaDetectionEnabled(enabled)
    }

    fun setDefaultVideoQuality(value: String) = launch {
        settingsRepository.setDefaultVideoQuality(value)
    }

    fun setDefaultAudioQuality(value: String) = launch {
        settingsRepository.setDefaultAudioQuality(value)
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
