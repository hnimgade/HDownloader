package com.hdownloader.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.designsystem.theme.ErrorRed
import com.hdownloader.core.designsystem.theme.SuccessGreen
import com.hdownloader.core.designsystem.theme.WarningAmber

@Composable
fun StatusBadge(
    status: DownloadStatus,
    modifier: Modifier = Modifier,
) {
    val container: Color
    val content: Color
    when (status) {
        DownloadStatus.COMPLETED -> {
            container = SuccessGreen.copy(alpha = 0.15f)
            content = SuccessGreen
        }
        DownloadStatus.FAILED -> {
            container = ErrorRed.copy(alpha = 0.15f)
            content = ErrorRed
        }
        DownloadStatus.DOWNLOADING -> {
            container = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            content = MaterialTheme.colorScheme.primary
        }
        DownloadStatus.PAUSED -> {
            container = WarningAmber.copy(alpha = 0.15f)
            content = WarningAmber
        }
        DownloadStatus.QUEUED, DownloadStatus.PREPARING -> {
            container = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            content = MaterialTheme.colorScheme.secondary
        }
        DownloadStatus.COMPLETING -> {
            container = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            content = MaterialTheme.colorScheme.primary
        }
        DownloadStatus.CANCELLED -> {
            container = MaterialTheme.colorScheme.surfaceVariant
            content = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Box(
        modifier = modifier
            .background(color = container, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
        )
    }
}

private val DownloadStatus.label: String
    get() = when (this) {
        DownloadStatus.QUEUED -> "Queued"
        DownloadStatus.PREPARING -> "Preparing"
        DownloadStatus.DOWNLOADING -> "Downloading"
        DownloadStatus.PAUSED -> "Paused"
        DownloadStatus.COMPLETING -> "Finalizing"
        DownloadStatus.COMPLETED -> "Completed"
        DownloadStatus.FAILED -> "Failed"
        DownloadStatus.CANCELLED -> "Cancelled"
    }
