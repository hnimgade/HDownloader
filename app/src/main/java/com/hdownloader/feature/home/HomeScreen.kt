package com.hdownloader.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.AddLink
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hdownloader.R
import com.hdownloader.core.designsystem.components.DownloadCard
import com.hdownloader.core.designsystem.components.SectionHeader

@Composable
fun HomeScreen(
    onOpenDownloads: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showAddUrlDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                HomeHeader(
                    activeCount = uiState.activeCount,
                    totalSpeed = uiState.totalSpeed,
                    onAddUrl = { showAddUrlDialog = true },
                )
            }

            item {
                QuickActions(onOpenDownloads = onOpenDownloads)
            }

            item {
                SectionHeader(title = "Active downloads") {
                    ViewAllAction(onOpenDownloads = onOpenDownloads)
                }
            }
            if (uiState.activeDownloads.isEmpty()) {
                item {
                    EmptyDownloadsHint(onAddUrl = { showAddUrlDialog = true })
                }
            } else {
                items(uiState.activeDownloads, key = { it.id }) { download ->
                    DownloadCard(
                        download = download,
                        onPauseResume = viewModel::pauseResume,
                        onCancel = viewModel::cancel,
                        onRetry = viewModel::retry,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
            }

            item {
                SectionHeader(title = "Recent downloads") {
                    ViewAllAction(onOpenDownloads = onOpenDownloads)
                }
            }
            items(uiState.recentDownloads, key = { it.id }) { download ->
                DownloadCard(
                    download = download,
                    onPauseResume = viewModel::pauseResume,
                    onCancel = viewModel::cancel,
                    onRetry = viewModel::retry,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }
        }
    }

    if (showAddUrlDialog) {
        AddUrlDialog(
            categories = categories,
            onDismiss = { showAddUrlDialog = false },
            onAdd = { url, categoryId, startImmediately ->
                viewModel.addDownload(url, categoryId, startImmediately)
                showAddUrlDialog = false
            },
        )
    }
}

@Composable
private fun ViewAllAction(onOpenDownloads: () -> Unit) {
    Text(
        text = "View all",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clickable { onOpenDownloads() },
    )
}

@Composable
private fun HomeHeader(
    activeCount: Int,
    totalSpeed: Long,
    onAddUrl: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "H Downloader logo",
                modifier = Modifier.size(44.dp),
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "H Downloader",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = greetingText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            onClick = onAddUrl,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddLink,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add download URL",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Paste a link to start downloading",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

private fun greetingText(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..21 -> "Good evening"
        else -> "Welcome back"
    }
}

@Composable
private fun QuickActions(onOpenDownloads: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickActionCard(
            title = "Downloads",
            subtitle = "Manage",
            icon = Icons.Rounded.Download,
            onClick = onOpenDownloads,
            modifier = Modifier.weight(1f),
        )
        QuickActionCard(
            title = "Playlists",
            subtitle = "Media",
            icon = Icons.AutoMirrored.Rounded.PlaylistPlay,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
        QuickActionCard(
            title = "Media",
            subtitle = "Library",
            icon = Icons.Rounded.LibraryMusic,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
        QuickActionCard(
            title = "Storage",
            subtitle = "Usage",
            icon = Icons.Rounded.Storage,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyDownloadsHint(onAddUrl: () -> Unit) {
    Card(
        onClick = onAddUrl,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = "No active downloads. Add a URL to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
