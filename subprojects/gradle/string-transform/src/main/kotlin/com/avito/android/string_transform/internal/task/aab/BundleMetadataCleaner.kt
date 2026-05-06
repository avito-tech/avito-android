package com.avito.android.string_transform.internal.task.aab

import com.avito.android.Result
import java.io.File

internal class BundleMetadataCleaner {

    fun clean(workspaceDirectory: File): Result<Unit> = Result.tryCatch {
        cleanMetaInf(workspaceDirectory.resolve("META-INF"))
        cleanBundleMetadata(workspaceDirectory.resolve("BUNDLE-METADATA"))
    }

    private fun cleanMetaInf(metadataDirectory: File) {
        if (!metadataDirectory.exists()) return

        metadataDirectory.walkBottomUp()
            .filter(File::isFile)
            .filter { file -> shouldDelete(file.relativeTo(metadataDirectory).invariantSeparatorsPath) }
            .forEach(File::delete)

        pruneEmptyDirectories(metadataDirectory)
    }

    private fun cleanBundleMetadata(bundleMetadataDirectory: File) {
        if (!bundleMetadataDirectory.exists()) return

        bundleMetadataDirectory.resolve(APP_DEPENDENCIES_RELATIVE_PATH)
            .takeIf(File::exists)
            ?.delete()

        pruneEmptyDirectories(bundleMetadataDirectory)
    }

    private fun pruneEmptyDirectories(root: File) {
        root.walkBottomUp()
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

    private companion object {
        const val APP_DEPENDENCIES_RELATIVE_PATH =
            "com.android.tools.build.libraries/dependencies.pb"
    }
}
