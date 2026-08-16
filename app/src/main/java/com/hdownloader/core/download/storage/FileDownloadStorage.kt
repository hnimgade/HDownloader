package com.hdownloader.core.download.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDownloadStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) : DownloadStorage {

    private val baseDir: File by lazy {
        (context.getExternalFilesDir(null) ?: context.filesDir).resolve(DIRECTORY_NAME)
    }

    override fun downloadsDirectory(): File {
        baseDir.mkdirs()
        return baseDir
    }

    override fun targetFile(fileName: String): File =
        File(downloadsDirectory(), fileName)

    override fun partFile(downloadId: Long, partIndex: Int): File =
        File(downloadsDirectory(), "$PART_PREFIX$downloadId.p$partIndex.tmp")

    override fun tempFile(fileName: String): File =
        File(downloadsDirectory(), "$TEMP_PREFIX$fileName.tmp")

    override fun deleteArtifacts(downloadId: Long, fileName: String) {
        val partPrefix = "$PART_PREFIX$downloadId."
        val tempName = tempFile(fileName).name
        downloadsDirectory().listFiles()?.forEach { file ->
            if (file.name.startsWith(partPrefix) || file.name == tempName) {
                file.delete()
            }
        }
    }

    override fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    private companion object {
        const val DIRECTORY_NAME = "downloads"
        const val PART_PREFIX = "part_"
        const val TEMP_PREFIX = ".download_"
    }
}
