package com.avito.android.string_transform.internal.task.aab

import com.avito.android.Result
import java.io.File

internal class BundleMetadataCleaner {

    fun clean(workspaceDirectory: File): Result<Unit> = Result.tryCatch {
        val metadataDirectory = workspaceDirectory.resolve("META-INF")
        if (!metadataDirectory.exists()) return@tryCatch

        metadataDirectory.walkBottomUp()
            .filter(File::isFile)
            .filter { file -> shouldDelete(file.relativeTo(metadataDirectory).invariantSeparatorsPath) }
            .forEach(File::delete)

        metadataDirectory.walkBottomUp()
            .filter(File::isDirectory)
            .filter { directory -> directory.list().isNullOrEmpty() }
            .forEach(File::delete)
    }

    private fun shouldDelete(relativePath: String): Boolean {
        val fileName = relativePath.substringAfterLast('/')
        return fileName == "MANIFEST.MF" ||
            fileName.endsWith(".SF") ||
            fileName.endsWith(".RSA") ||
            fileName.endsWith(".DSA") ||
            fileName.startsWith("BNDLTOOL.")
    }
}
