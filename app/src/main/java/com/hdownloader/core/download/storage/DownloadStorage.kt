package com.hdownloader.core.download.storage

import java.io.File

/**
 * Resolves where downloaded bytes are written on disk.
 *
 * Segmented downloads write one temporary part file per connection and the
 * parts are concatenated into the final target on completion. Non-segmented
 * downloads stream directly into a temporary file which is then renamed.
 */
interface DownloadStorage {

    /** Directory that holds downloaded files. */
    fun downloadsDirectory(): File

    /** Final destination for a completed download. */
    fun targetFile(fileName: String): File

    /** Temporary file for a single part of a segmented download. */
    fun partFile(downloadId: Long, partIndex: Int): File

    /** Temporary file used while a non-segmented download is in flight. */
    fun tempFile(fileName: String): File

    /** Deletes all on-disk artefacts for a download (partial + merged temp). */
    fun deleteArtifacts(downloadId: Long, fileName: String)

    /** Deletes a previously recorded file, if present. */
    fun deleteFile(path: String?)
}
