package com.hdownloader.core.download.engine

import com.hdownloader.core.common.DispatchersProvider
import com.hdownloader.core.database.dao.DownloadDao
import com.hdownloader.core.database.dao.DownloadPartDao
import com.hdownloader.core.database.entity.DownloadEntity
import com.hdownloader.core.database.entity.DownloadPartEntity
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.download.storage.DownloadStorage
import com.hdownloader.core.history.model.DownloadHistory
import com.hdownloader.core.history.repository.DownloadHistoryRepository
import com.hdownloader.core.media.model.Media
import com.hdownloader.core.media.repository.MediaRepository
import com.hdownloader.core.network.UrlParser
import com.hdownloader.core.settings.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates a single download end to end:
 *
 * 1. PREPARING - HEAD / range probe to discover size, resume support, filename.
 * 2. DOWNLOADING - one stream per part; part offsets are persisted so the
 *    transfer resumes after a pause or crash when the server supports ranges.
 * 3. COMPLETING - part files merged into the final destination.
 * 4. COMPLETED - download history and media library entries are recorded.
 *
 * Status mutations go through [DownloadStateMachine]; external pause/cancel is
 * signalled via [DownloadCancellation] and a [CancellationException], after
 * which the engine leaves the status (PAUSED/CANCELLED) owned by the caller.
 */
@Singleton
class DownloadEngine @Inject constructor(
    private val downloadDao: DownloadDao,
    private val partDao: DownloadPartDao,
    private val downloader: HttpDownloader,
    private val storage: DownloadStorage,
    private val historyRepository: DownloadHistoryRepository,
    private val mediaRepository: MediaRepository,
    private val settingsRepository: AppSettingsRepository,
    private val cancellation: DownloadCancellation,
    private val dispatchers: DispatchersProvider,
) {

    suspend fun execute(id: Long) {
        val entity = downloadDao.getById(id) ?: return
        if (entity.status != DownloadStatus.QUEUED) return
        withContext(dispatchers.io) {
            runDownload(id, entity)
        }
    }

    private suspend fun runDownload(id: Long, entity: DownloadEntity) {
        val settings = settingsRepository.settings.first()
        val token = cancellation.register(id)
        try {
            prepareAndTransfer(id, entity, settings.browserUserAgent, token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fail(id, e)
        } finally {
            cancellation.unregister(id)
        }
    }

    private suspend fun prepareAndTransfer(
        id: Long,
        entity: DownloadEntity,
        userAgent: String,
        token: AtomicBoolean,
    ) {
        downloadDao.updateStatus(id, DownloadStatus.PREPARING, false)
        val metadata = downloader.fetchMetadata(entity.url, userAgent)
        if (downloadDao.getById(id)?.status != DownloadStatus.PREPARING) return

        val fileName = sanitizeFileName(metadata.contentDispositionFileName ?: entity.fileName)
        val supportsRange = metadata.acceptsRanges && metadata.totalBytes > 0
        val connections = if (supportsRange) {
            decideConnections(metadata.totalBytes, settingsRepository.settings.first().maxConnectionsPerDownload)
        } else {
            1
        }
        val target = storage.targetFile(fileName)
        storage.downloadsDirectory().mkdirs()

        downloadDao.updateMeta(
            id = id,
            finalUrl = metadata.finalUrl,
            totalBytes = metadata.totalBytes,
            supportsRange = supportsRange,
            connectionCount = connections,
            filePath = target.absolutePath,
            mimeType = metadata.mimeType,
        )

        if (!supportsRange && entity.downloadedBytes > 0L) {
            downloadDao.resetProgress(id, System.currentTimeMillis())
            storage.deleteArtifacts(id, fileName)
        }

        val parts = buildParts(id, supportsRange, metadata.totalBytes, connections)
        downloadDao.updateStartedAt(id, System.currentTimeMillis())
        downloadDao.updateStatus(id, DownloadStatus.DOWNLOADING, false)

        val progress = ProgressReporter(downloadDao, partDao, id, parts, metadata.totalBytes)
        try {
            transferParts(id, parts, metadata.finalUrl, userAgent, fileName, supportsRange, token, progress)
            if (token.get()) throw CancellationException("Download $id cancelled")
            if (supportsRange) {
                val downloaded = progress.downloadedBytes()
                if (downloaded != metadata.totalBytes) {
                    throw HttpDownloader.HttpDownloadException(
                        "Incomplete download: $downloaded of ${metadata.totalBytes} bytes",
                    )
                }
            }
            progress.flush()
            finalize(id, parts, fileName, metadata.totalBytes, supportsRange, target)
            complete(id, fileName, target, metadata.totalBytes, metadata.mimeType, metadata.finalUrl, entity.url)
        } catch (e: CancellationException) {
            progress.flush()
            throw e
        }
    }

    private suspend fun transferParts(
        id: Long,
        parts: List<DownloadPartEntity>,
        url: String,
        userAgent: String,
        fileName: String,
        isSegmented: Boolean,
        token: AtomicBoolean,
        progress: ProgressReporter,
    ) {
        coroutineScope {
            parts.forEach { part ->
                launch(dispatchers.io) {
                    transferPart(id, part, url, userAgent, fileName, isSegmented, token, progress)
                }
            }
        }
    }

    private suspend fun transferPart(
        id: Long,
        part: DownloadPartEntity,
        url: String,
        userAgent: String,
        fileName: String,
        isSegmented: Boolean,
        token: AtomicBoolean,
        progress: ProgressReporter,
    ) {
        if (isSegmented && part.currentByte > part.endByte) return
        val file = if (isSegmented) storage.partFile(id, part.partIndex) else storage.tempFile(fileName)
        RandomAccessFile(file, "rw").use { raf ->
            var offset = part.currentByte
            raf.seek(offset - part.startByte)
            val end = if (isSegmented) part.endByte else null
            downloader.streamRange(url, offset, end, userAgent) { chunk ->
                if (token.get()) throw CancellationException("Download $id cancelled")
                raf.write(chunk)
                offset += chunk.size
                progress.report(part.id, offset)
            }
            progress.report(part.id, offset)
        }
    }

    private suspend fun buildParts(
        id: Long,
        supportsRange: Boolean,
        totalBytes: Long,
        connections: Int,
    ): List<DownloadPartEntity> {
        if (!supportsRange) {
            partDao.deleteByDownloadId(id)
            val fresh = listOf(
                DownloadPartEntity(
                    downloadId = id,
                    partIndex = 0,
                    startByte = 0L,
                    endByte = -1L,
                    currentByte = 0L,
                    status = DownloadStatus.QUEUED,
                ),
            )
            partDao.upsertAll(fresh)
            return partDao.getParts(id)
        }

        val plan = splitRanges(totalBytes, connections)
        val existing = partDao.getParts(id)
        val reusable = existing.size == plan.size &&
            existing.zip(plan).all { (entity, spec) ->
                entity.partIndex == spec.index && entity.startByte == spec.start && entity.endByte == spec.end
            }
        return if (reusable) {
            existing.map { it.copy(status = DownloadStatus.QUEUED) }.also { partDao.upsertAll(it) }
        } else {
            partDao.deleteByDownloadId(id)
            val fresh = plan.map { spec ->
                DownloadPartEntity(
                    downloadId = id,
                    partIndex = spec.index,
                    startByte = spec.start,
                    endByte = spec.end,
                    currentByte = spec.start,
                    status = DownloadStatus.QUEUED,
                )
            }
            partDao.upsertAll(fresh)
            partDao.getParts(id)
        }
    }

    private fun splitRanges(totalBytes: Long, connections: Int): List<PartSpec> {
        val count = connections.coerceAtLeast(1)
        val segment = (totalBytes + count - 1) / count
        return (0 until count).map { index ->
            val start = index * segment
            val end = minOf(start + segment - 1, totalBytes - 1)
            PartSpec(index = index, start = start, end = end)
        }
    }

    private fun decideConnections(totalBytes: Long, maxConnections: Int): Int {
        if (totalBytes <= 0L) return 1
        val maxParts = ((totalBytes + MIN_SEGMENT_BYTES - 1) / MIN_SEGMENT_BYTES).toInt().coerceAtLeast(1)
        return minOf(maxConnections.coerceAtLeast(1), maxParts).coerceAtLeast(1)
    }

    private suspend fun finalize(
        id: Long,
        parts: List<DownloadPartEntity>,
        fileName: String,
        totalBytes: Long,
        isSegmented: Boolean,
        target: File,
    ) {
        if (isSegmented) {
            mergeSegments(id, parts, target)
        } else {
            finalizeSingle(fileName, target)
        }
        downloadDao.updateProgress(id, totalBytes, 0L, 0L)
    }

    private fun mergeSegments(id: Long, parts: List<DownloadPartEntity>, target: File) {
        FileOutputStream(target).use { out ->
            parts.sortedBy { it.partIndex }.forEach { part ->
                val partFile = storage.partFile(id, part.partIndex)
                if (!partFile.exists() || partFile.length() <= 0L) {
                    throw IOException("Missing part file for download $id part ${part.partIndex}")
                }
                partFile.inputStream().use { it.copyTo(out) }
            }
        }
        parts.forEach { storage.partFile(id, it.partIndex).delete() }
    }

    private fun finalizeSingle(fileName: String, target: File) {
        val temp = storage.tempFile(fileName)
        if (!temp.exists()) throw IOException("Missing temporary file for $fileName")
        Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    private suspend fun complete(
        id: Long,
        fileName: String,
        target: File,
        totalBytes: Long,
        mimeType: String?,
        finalUrl: String,
        originalUrl: String,
    ) {
        downloadDao.updateStatus(id, DownloadStatus.COMPLETING, false)
        if (downloadDao.getById(id)?.status != DownloadStatus.COMPLETING) return
        downloadDao.updateCompleted(id, target.absolutePath, System.currentTimeMillis())
        downloadDao.updateProgress(id, totalBytes, 0L, 0L)
        downloadDao.updateStatus(id, DownloadStatus.COMPLETED, false)

        val sourceHost = UrlParser.host(originalUrl)
        runCatching {
            historyRepository.record(
                DownloadHistory(
                    url = finalUrl,
                    fileName = fileName,
                    mimeType = mimeType,
                    size = totalBytes,
                    sourceHost = sourceHost,
                    status = DownloadStatus.COMPLETED,
                ),
            )
        }
        runCatching {
            mediaRepository.upsert(
                Media(
                    fileName = fileName,
                    filePath = target.absolutePath,
                    mimeType = mimeType,
                    size = totalBytes,
                    category = MediaCategorizer.forFileName(fileName, mimeType),
                ),
            )
        }
    }

    private suspend fun fail(id: Long, error: Throwable) {
        val current = downloadDao.getById(id)?.status ?: return
        if (current == DownloadStatus.PAUSED || current == DownloadStatus.CANCELLED) return
        if (DownloadStateMachine.canTransition(current, DownloadStatus.FAILED)) {
            downloadDao.updateStatus(id, DownloadStatus.FAILED, false)
            downloadDao.updateError(id, error.message ?: error.javaClass.simpleName)
        }
    }

    private fun sanitizeFileName(name: String): String {
        val clean = name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
        return clean.ifBlank { DEFAULT_FILE_NAME }
    }

    private data class PartSpec(val index: Int, val start: Long, val end: Long)

    /**
     * Throttles progress writes to the database and tracks per-part offsets so
     * a paused download can be resumed exactly where it stopped.
     */
    private class ProgressReporter(
        private val downloadDao: DownloadDao,
        private val partDao: DownloadPartDao,
        private val id: Long,
        private val parts: List<DownloadPartEntity>,
        private val totalBytes: Long,
    ) {
        private val partOffsets = ConcurrentHashMap<Long, Long>()
        private val partStarts = parts.associate { it.id to it.startByte }
        private var lastEmit = 0L
        private var lastBytes = 0L
        private var lastTime = 0L
        private var speed = 0L

        init {
            parts.forEach { partOffsets[it.id] = it.currentByte }
        }

        fun downloadedBytes(): Long {
            var sum = 0L
            partOffsets.forEach { (partId, offset) ->
                sum += (offset - (partStarts[partId] ?: 0L)).coerceAtLeast(0L)
            }
            return sum
        }

        suspend fun report(partId: Long, currentByte: Long) {
            partOffsets[partId] = currentByte
            val now = System.currentTimeMillis()
            val downloaded = downloadedBytes()
            if (lastTime > 0L && now > lastTime) {
                speed = ((downloaded - lastBytes) * 1000L / (now - lastTime)).coerceAtLeast(0L)
            }
            lastTime = now
            lastBytes = downloaded
            if (now - lastEmit >= PROGRESS_INTERVAL_MS) {
                lastEmit = now
                partDao.updateProgress(partId, currentByte, DownloadStatus.DOWNLOADING)
                emitDownload(downloaded)
            }
        }

        suspend fun flush() {
            parts.forEach {
                partDao.updateProgress(it.id, partOffsets[it.id] ?: it.currentByte, DownloadStatus.DOWNLOADING)
            }
            emitDownload(downloadedBytes())
        }

        private suspend fun emitDownload(downloaded: Long) {
            val remaining = (totalBytes - downloaded).coerceAtLeast(0L)
            val eta = if (speed > 0L) remaining / speed else 0L
            downloadDao.updateProgress(id, downloaded, speed, eta)
        }
    }

    private companion object {
        const val MIN_SEGMENT_BYTES = 32L * 1024L
        const val PROGRESS_INTERVAL_MS = 250L
        const val DEFAULT_FILE_NAME = "download.bin"
    }
}
