package com.avito.android.string_transform.internal.task.common

import com.avito.android.Result
import java.io.File
import java.io.FileOutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal class BinaryArchiver {

    fun unpackToWorkspace(inputArchive: File, workspaceDirectory: File): Result<CompressionManifest> = Result.tryCatch {
        workspaceDirectory.mkdirs()

        val methods = mutableMapOf<String, Int>()

        ZipFile(inputArchive).use { zipFile ->
            for (entry in zipFile.entries()) {
                val output = workspaceDirectory.resolve(entry.name).canonicalFile
                check(output.toPath().startsWith(workspaceDirectory.canonicalFile.toPath())) {
                    "Archive entry resolves outside workspace: ${entry.name}"
                }
                if (entry.isDirectory) {
                    output.mkdirs()
                } else {
                    output.parentFile?.mkdirs()
                    zipFile.getInputStream(entry).use { input ->
                        output.outputStream().use { out -> input.copyTo(out) }
                    }
                    val relPath = output.relativeTo(workspaceDirectory.canonicalFile).invariantSeparatorsPath
                    methods[relPath] = entry.method
                }
            }
        }

        CompressionManifest(methods)
    }

    fun packFromWorkspace(
        workspaceDirectory: File,
        outputArchive: File,
        manifest: CompressionManifest,
    ): Result<Unit> = Result.tryCatch {
        outputArchive.parentFile.mkdirs()

        ZipOutputStream(FileOutputStream(outputArchive)).use { zip ->
            workspaceDirectory.walkTopDown()
                .filter(File::isFile)
                .sortedBy { it.relativeTo(workspaceDirectory).invariantSeparatorsPath }
                .forEach { file ->
                    val entryName = file.relativeTo(workspaceDirectory).invariantSeparatorsPath
                    writeEntry(zip, file, entryName, resolveMethod(entryName, manifest))
                }
        }
    }

    private fun resolveMethod(entryName: String, manifest: CompressionManifest): Int {
        // Android 11+ rejects installs whose resources.arsc is DEFLATED
        // (INSTALL_PARSE_FAILED_RESOURCES_ARSC_COMPRESSED), so always emit it STORED.
        if (entryName == APK_RESOURCES_ARSC) return ZipEntry.STORED
        return manifest.methodFor(entryName) ?: ZipEntry.DEFLATED
    }

    private fun writeEntry(zip: ZipOutputStream, file: File, entryName: String, method: Int) {
        val entry = ZipEntry(entryName).apply { this.method = method }
        if (method == ZipEntry.STORED) {
            val crc = CRC32()
            file.inputStream().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    crc.update(buffer, 0, bytesRead)
                }
            }
            entry.size = file.length()
            entry.compressedSize = file.length()
            entry.crc = crc.value
        }
        zip.putNextEntry(entry)
        file.inputStream().use { input -> input.copyTo(zip) }
        zip.closeEntry()
    }

    private companion object {
        const val APK_RESOURCES_ARSC = "resources.arsc"
    }
}
