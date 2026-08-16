package com.hdownloader.core.download.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Surfaces a lightweight summary notification while downloads are in flight.
 * Best-effort only: failures to post are swallowed so download progress is
 * never blocked by notification issues.
 */
@Singleton
class DownloadNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun post(active: Int, failed: Int) {
        if (active <= 0) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }
        val content = if (failed > 0) {
            "$active active, $failed failed download(s)"
        } else {
            "$active active download(s)"
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(TITLE)
            .setContentText(content)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        runCatching { notificationManager.notify(NOTIFICATION_ID, notification) }
    }

    private companion object {
        const val CHANNEL_ID = "downloads"
        const val CHANNEL_NAME = "Downloads"
        const val NOTIFICATION_ID = 1001
        const val TITLE = "HDownloader"
    }
}
