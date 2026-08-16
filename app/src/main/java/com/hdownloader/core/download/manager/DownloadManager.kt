package com.hdownloader.core.download.manager

import com.hdownloader.core.common.DispatchersProvider
import com.hdownloader.core.database.dao.DownloadDao
import com.hdownloader.core.database.entity.DownloadEntity
import com.hdownloader.core.download.engine.DownloadCancellation
import com.hdownloader.core.download.engine.DownloadEngine
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.download.notification.DownloadNotifier
import com.hdownloader.core.download.storage.DownloadStorage
import com.hdownloader.core.settings.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide coordinator that turns persisted download rows into running
 * transfers. It observes the database as the single source of truth:
 *
 * - QUEUED rows become engine jobs, bounded by [AppSettings.maxConcurrentDownloads].
 * - PAUSED/CANCELLED rows cancel their running job (progress is flushed by the
 *   engine) and cancelled downloads have their partial files removed.
 * - FAILED rows are re-queued up to [MAX_RETRIES] when auto-retry is enabled.
 */
@Singleton
class DownloadManager @Inject constructor(
    private val downloadDao: DownloadDao,
    private val engine: DownloadEngine,
    private val cancellation: DownloadCancellation,
    private val storage: DownloadStorage,
    private val notifier: DownloadNotifier,
    private val settingsRepository: AppSettingsRepository,
    private val dispatchers: DispatchersProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val activeJobs = ConcurrentHashMap<Long, Job>()
    private val drainMutex = Mutex()

    @Volatile
    private var observerStarted = false

    /** Starts the reactive observer. Safe to call more than once. */
    fun startObserver() {
        if (observerStarted) return
        observerStarted = true
        scope.launch {
            combine(settingsRepository.settings, downloadDao.observeAll()) { settings, downloads ->
                settings to downloads
            }
                .collect { (settings, downloads) ->
                    drainOnce(settings.maxConcurrentDownloads, downloads)
                }
        }
    }

    /**
     * Single-pass drain, used by [com.hdownloader.core.download.worker.DownloadWorker]
     * and app startup so transfers are attempted even without a live observer.
     */
    suspend fun processPending() {
        val settings = settingsRepository.settings.first()
        drainOnce(settings.maxConcurrentDownloads, downloadDao.getAll())
    }

    private suspend fun drainOnce(maxConcurrent: Int, downloads: List<DownloadEntity>) {
        drainMutex.withLock {
            val byId = downloads.associateBy { it.id }

            activeJobs.keys.toList().forEach { id ->
                val status = byId[id]?.status
                if (status == null || !status.isActive) {
                    cancellation.requestCancellation(id)
                    activeJobs.remove(id)?.let { job ->
                        job.cancel()
                        withTimeoutOrNull(JOB_JOIN_TIMEOUT_MS) { job.join() }
                    }
                    cleanupIfNeeded(byId[id])
                }
            }

            val queued = downloads.filter {
                it.status == DownloadStatus.QUEUED && !activeJobs.containsKey(it.id)
            }
            for (download in queued) {
                if (activeJobs.size >= maxConcurrent) break
                startJob(download.id)
            }

            notifier.post(activeJobs.size, downloads.count { it.status == DownloadStatus.FAILED })
        }
    }

    private fun startJob(id: Long) {
        val job = scope.launch {
            try {
                engine.execute(id)
                maybeRetry(id)
            } catch (e: CancellationException) {
                // Intentional pause/cancel; nothing to do.
            } finally {
                activeJobs.remove(id)
                notifier.post(activeJobs.size, 0)
            }
        }
        activeJobs[id] = job
    }

    private suspend fun maybeRetry(id: Long) {
        val entity = downloadDao.getById(id) ?: return
        if (entity.status != DownloadStatus.FAILED) return
        val settings = settingsRepository.settings.first()
        if (!settings.autoRetry || entity.retryCount >= MAX_RETRIES) return
        downloadDao.updateRetryCount(id, entity.retryCount + 1)
        downloadDao.updateStatus(id, DownloadStatus.QUEUED, false)
    }

    private suspend fun cleanupIfNeeded(entity: DownloadEntity?) {
        if (entity?.status == DownloadStatus.CANCELLED) {
            storage.deleteArtifacts(entity.id, entity.fileName)
        }
    }

    private companion object {
        const val MAX_RETRIES = 3
        const val JOB_JOIN_TIMEOUT_MS = 5_000L
    }
}
