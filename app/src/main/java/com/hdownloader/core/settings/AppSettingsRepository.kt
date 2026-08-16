package com.hdownloader.core.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val maxConcurrentDownloads: Int = 3,
    val maxConnectionsPerDownload: Int = 4,
    val autoRetry: Boolean = true,
    val wifiOnly: Boolean = false,
    val autoStartDownloads: Boolean = true,
    val defaultDownloadLocation: String = "Download/HDownloader",
    val clipboardDetectionEnabled: Boolean = false,
    val browserMediaDetectionEnabled: Boolean = true,
    val browserHomepage: String = "https://www.google.com",
    val browserSearchEngine: String = "Google",
    val browserUserAgent: String = "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36",
    val defaultVideoQuality: String = "720p",
    val defaultAudioQuality: String = "192 kbps",
)

private val Context.dataStore by preferencesDataStore(name = "h_downloader_settings")

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val MAX_CONCURRENT = intPreferencesKey("max_concurrent_downloads")
        val MAX_CONNECTIONS = intPreferencesKey("max_connections")
        val AUTO_RETRY = booleanPreferencesKey("auto_retry")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val AUTO_START = booleanPreferencesKey("auto_start")
        val DOWNLOAD_LOCATION = stringPreferencesKey("download_location")
        val CLIPBOARD_DETECTION = booleanPreferencesKey("clipboard_detection")
        val BROWSER_MEDIA_DETECTION = booleanPreferencesKey("browser_media_detection")
        val BROWSER_HOMEPAGE = stringPreferencesKey("browser_homepage")
        val BROWSER_SEARCH_ENGINE = stringPreferencesKey("browser_search_engine")
        val BROWSER_USER_AGENT = stringPreferencesKey("browser_user_agent")
        val DEFAULT_VIDEO_QUALITY = stringPreferencesKey("default_video_quality")
        val DEFAULT_AUDIO_QUALITY = stringPreferencesKey("default_audio_quality")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME_MODE]
                ?.let { mode -> ThemeMode.entries.firstOrNull { it.name == mode } }
                ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            maxConcurrentDownloads = prefs[Keys.MAX_CONCURRENT] ?: 3,
            maxConnectionsPerDownload = prefs[Keys.MAX_CONNECTIONS] ?: 4,
            autoRetry = prefs[Keys.AUTO_RETRY] ?: true,
            wifiOnly = prefs[Keys.WIFI_ONLY] ?: false,
            autoStartDownloads = prefs[Keys.AUTO_START] ?: true,
            defaultDownloadLocation = prefs[Keys.DOWNLOAD_LOCATION] ?: "Download/HDownloader",
            clipboardDetectionEnabled = prefs[Keys.CLIPBOARD_DETECTION] ?: false,
            browserMediaDetectionEnabled = prefs[Keys.BROWSER_MEDIA_DETECTION] ?: true,
            browserHomepage = prefs[Keys.BROWSER_HOMEPAGE] ?: "https://www.google.com",
            browserSearchEngine = prefs[Keys.BROWSER_SEARCH_ENGINE] ?: "Google",
            browserUserAgent =
                prefs[Keys.BROWSER_USER_AGENT] ?: "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36",
            defaultVideoQuality = prefs[Keys.DEFAULT_VIDEO_QUALITY] ?: "720p",
            defaultAudioQuality = prefs[Keys.DEFAULT_AUDIO_QUALITY] ?: "192 kbps",
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit {
        it[Keys.THEME_MODE] = mode.name
    }

    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit {
        it[Keys.DYNAMIC_COLOR] = enabled
    }

    suspend fun setMaxConcurrentDownloads(value: Int) = context.dataStore.edit {
        it[Keys.MAX_CONCURRENT] = value
    }

    suspend fun setMaxConnectionsPerDownload(value: Int) = context.dataStore.edit {
        it[Keys.MAX_CONNECTIONS] = value
    }

    suspend fun setAutoRetry(enabled: Boolean) = context.dataStore.edit {
        it[Keys.AUTO_RETRY] = enabled
    }

    suspend fun setWifiOnly(enabled: Boolean) = context.dataStore.edit {
        it[Keys.WIFI_ONLY] = enabled
    }

    suspend fun setAutoStartDownloads(enabled: Boolean) = context.dataStore.edit {
        it[Keys.AUTO_START] = enabled
    }

    suspend fun setDefaultDownloadLocation(value: String) = context.dataStore.edit {
        it[Keys.DOWNLOAD_LOCATION] = value
    }

    suspend fun setClipboardDetectionEnabled(enabled: Boolean) = context.dataStore.edit {
        it[Keys.CLIPBOARD_DETECTION] = enabled
    }

    suspend fun setBrowserMediaDetectionEnabled(enabled: Boolean) = context.dataStore.edit {
        it[Keys.BROWSER_MEDIA_DETECTION] = enabled
    }

    suspend fun setBrowserHomepage(value: String) = context.dataStore.edit {
        it[Keys.BROWSER_HOMEPAGE] = value
    }

    suspend fun setBrowserSearchEngine(value: String) = context.dataStore.edit {
        it[Keys.BROWSER_SEARCH_ENGINE] = value
    }

    suspend fun setBrowserUserAgent(value: String) = context.dataStore.edit {
        it[Keys.BROWSER_USER_AGENT] = value
    }

    suspend fun setDefaultVideoQuality(value: String) = context.dataStore.edit {
        it[Keys.DEFAULT_VIDEO_QUALITY] = value
    }

    suspend fun setDefaultAudioQuality(value: String) = context.dataStore.edit {
        it[Keys.DEFAULT_AUDIO_QUALITY] = value
    }
}
