package com.hdownloader.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hdownloader.core.download.model.DownloadState
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.util.FileSizeFormatter

/**
 * Renders a single download row, used by Home and Downloads screens.
 */
@Composable
fun DownloadCard(
    download: DownloadState,
    modifier: Modifier = Modifier,
    onPauseResume: (DownloadState) -> Unit = {},
    onCancel: (DownloadState) -> Unit = {},
    onRetry: (DownloadState) -> Unit = {},
    onClick: (DownloadState) -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick(download) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = download.subtitle(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = download.sourceUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                StatusBadge(status = download.status)
            }

            if (download.status == DownloadStatus.DOWNLOADING ||
                download.status == DownloadStatus.PREPARING
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { download.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(
                        text = "${(download.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${FileSizeFormatter.format(download.speed)}/s",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = download.etaText(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (download.status == DownloadStatus.FAILED && download.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = download.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (download.status.isPausable || download.status.isResumable) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onPauseResume(download) }) {
                        Icon(
                            imageVector = if (download.status.isPausable) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (download.status.isPausable) "Pause" else "Resume",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (download.status == DownloadStatus.FAILED) {
                        IconButton(onClick = { onRetry(download) }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Retry",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DownloadState.subtitle(): String {
    val sizeText = if (totalBytes > 0) {
        "${FileSizeFormatter.format(downloadedBytes)} / ${FileSizeFormatter.format(totalBytes)}"
    } else {
        FileSizeFormatter.format(downloadedBytes)
    }
    return sizeText
}

private fun DownloadState.etaText(): String {
    if (eta <= 0) return "—"
    val seconds = eta / 1000
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return when {
        h > 0 -> "${h}h ${m}m left"
        m > 0 -> "${m}m ${seconds % 60}s left"
        else -> "${seconds}s left"
    }
}
