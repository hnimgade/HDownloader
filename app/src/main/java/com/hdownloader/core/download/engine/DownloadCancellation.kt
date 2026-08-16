package com.hdownloader.core.download.engine

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cancellation flags, one per active download. The coordinator sets
 * the flag when a download is paused or cancelled so the engine can stop
 * cooperatively between chunks, regardless of how its coroutine job is
 * cancelled.
 */
@Singleton
class DownloadCancellation @Inject constructor() {

    private val flags = ConcurrentHashMap<Long, AtomicBoolean>()

    fun register(id: Long): AtomicBoolean =
        flags.computeIfAbsent(id) { AtomicBoolean(false) }

    fun requestCancellation(id: Long) {
        flags[id]?.set(true)
    }

    fun isCancelled(id: Long): Boolean = flags[id]?.get() ?: false

    fun unregister(id: Long) {
        flags.remove(id)
    }
}
