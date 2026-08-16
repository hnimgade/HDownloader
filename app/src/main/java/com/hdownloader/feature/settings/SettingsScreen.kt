package com.hdownloader.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hdownloader.core.settings.ThemeMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp),
        ) {
            item { SettingsSectionTitle("Appearance") }
            item {
                SettingsGroup {
                    SettingsRow(
                        title = "Theme",
                        subtitle = settings.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                        icon = Icons.Rounded.Palette,
                    ) {
                        ThemeSelector(
                            current = settings.themeMode,
                            onSelect = viewModel::setThemeMode,
                        )
                    }
                    SettingsRow(
                        title = "Dynamic colors",
                        subtitle = "Use Android 12+ dynamic color scheme",
                        icon = Icons.Rounded.AutoAwesome,
                    ) {
                        Switch(
                            checked = settings.dynamicColor,
                            onCheckedChange = viewModel::setDynamicColor,
                        )
                    }
                }
            }

            item { SettingsSectionTitle("Downloads") }
            item {
                SettingsGroup {
                    SliderSetting(
                        title = "Max concurrent downloads",
                        subtitle = settings.maxConcurrentDownloads.toString(),
                        value = settings.maxConcurrentDownloads.toFloat(),
                        valueRange = 1f..10f,
                        onValueChange = { viewModel.setMaxConcurrentDownloads(it.toInt()) },
                    )
                    SliderSetting(
                        title = "Connections per download",
                        subtitle = settings.maxConnectionsPerDownload.toString(),
                        value = settings.maxConnectionsPerDownload.toFloat(),
                        valueRange = 1f..16f,
                        onValueChange = { viewModel.setMaxConnections(it.toInt()) },
                    )
                    SettingsRow(
                        title = "Auto retry",
                        subtitle = "Retry failed downloads automatically",
                        icon = Icons.Rounded.Download,
                    ) {
                        Switch(
                            checked = settings.autoRetry,
                            onCheckedChange = viewModel::setAutoRetry,
                        )
                    }
                    SettingsRow(
                        title = "Wi-Fi only",
                        subtitle = "Only download on Wi-Fi networks",
                        icon = Icons.Rounded.Cloud,
                    ) {
                        Switch(
                            checked = settings.wifiOnly,
                            onCheckedChange = viewModel::setWifiOnly,
                        )
                    }
                    SettingsRow(
                        title = "Auto start",
                        subtitle = "Start downloads immediately when added",
                        icon = Icons.Rounded.Download,
                    ) {
                        Switch(
                            checked = settings.autoStartDownloads,
                            onCheckedChange = viewModel::setAutoStart,
                        )
                    }
                }
            }

            item { SettingsSectionTitle("Browser") }
            item {
                SettingsGroup {
                    SettingsRow(
                        title = "Media detection",
                        subtitle = "Detect downloadable media while browsing",
                        icon = Icons.Rounded.Public,
                    ) {
                        Switch(
                            checked = settings.browserMediaDetectionEnabled,
                            onCheckedChange = viewModel::setBrowserMediaDetection,
                        )
                    }
                }
            }

            item { SettingsSectionTitle("Clipboard") }
            item {
                SettingsGroup {
                    SettingsRow(
                        title = "Clipboard detection",
                        subtitle = "Suggest downloading copied URLs (opt-in)",
                        icon = Icons.Rounded.Download,
                    ) {
                        Switch(
                            checked = settings.clipboardDetectionEnabled,
                            onCheckedChange = viewModel::setClipboardDetection,
                        )
                    }
                }
            }

            item { SettingsSectionTitle("Media") }
            item {
                SettingsGroup {
                    QualityDropdown(
                        title = "Default video quality",
                        options = listOf("1080p", "720p", "480p", "360p"),
                        current = settings.defaultVideoQuality,
                        onSelect = viewModel::setDefaultVideoQuality,
                        icon = Icons.Rounded.Movie,
                    )
                    QualityDropdown(
                        title = "Default audio quality",
                        options = listOf("320 kbps", "192 kbps", "128 kbps"),
                        current = settings.defaultAudioQuality,
                        onSelect = viewModel::setDefaultAudioQuality,
                        icon = Icons.Rounded.Movie,
                    )
                }
            }

            item { SettingsSectionTitle("About") }
            item {
                SettingsGroup {
                    SettingsRow(
                        title = "About H Downloader",
                        subtitle = "Version 1.0.0",
                        icon = Icons.Rounded.Download,
                        onClick = onOpenAbout,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 4.dp, end = 24.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        content = { content() },
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing()
        if (onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SliderSetting(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

@Composable
private fun ThemeSelector(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { mode ->
            OutlinedButton(
                onClick = { onSelect(mode) },
                modifier = Modifier,
            ) {
                Text(
                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (mode == current) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun QualityDropdown(
    title: String,
    options: List<String>,
    current: String,
    onSelect: (String) -> Unit,
    icon: ImageVector,
) {
    var expanded by remember { mutableStateOf(false) }
    SettingsRow(
        title = title,
        subtitle = current,
        icon = icon,
    ) {
        androidx.compose.material3.IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Choose quality",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
