package com.avito.android.string_transform.internal.task.common

import com.avito.android.Result
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

internal class BinaryArchiver {

    fun unpackToWorkspace(inputArchive: File, workspaceDirectory: File): Result<Unit> = Result.tryCatch {
        workspaceDirectory.mkdirs()

        ZipInputStream(FileInputStream(inputArchive)).use { zip ->
            generateSequence { zip.nextEntry }
                .forEach { entry ->
                    val output = workspaceDirectory.resolve(entry.name).canonicalFile
                    check(output.toPath().startsWith(workspaceDirectory.canonicalFile.toPath())) {
                        "Archive entry resolves outside workspace: ${entry.name}"
                    }
                    if (entry.isDirectory) {
                        output.mkdirs()
                    } else {
                        output.parentFile?.mkdirs()
                        output.outputStream().use(zip::copyTo)
                    }
                    zip.closeEntry()
                }
        }
    }

    fun packFromWorkspace(workspaceDirectory: File, outputArchive: File): Result<Unit> = Result.tryCatch {
        outputArchive.parentFile.mkdirs()

        ZipOutputStream(FileOutputStream(outputArchive)).use { zip ->
            workspaceDirectory.walkTopDown()
                .filter(File::isFile)
                .sortedBy { it.relativeTo(workspaceDirectory).invariantSeparatorsPath }
                .forEach { file ->
                    val entryName = file.relativeTo(workspaceDirectory).invariantSeparatorsPath
                    writeEntry(zip, file, entryName)
                }
        }
    }

    private fun writeEntry(zip: ZipOutputStream, file: File, entryName: String) {
        if (entryName == APK_RESOURCES_ARSC) {
            // Android 11+ rejects installs whose `resources.arsc` is DEFLATED
            // (INSTALL_PARSE_FAILED_RESOURCES_ARSC_COMPRESSED), so emit it STORED.
            val bytes = file.readBytes()
            val crc = CRC32().apply { update(bytes) }
            val entry = ZipEntry(entryName).apply {
                method = ZipEntry.STORED
                size = bytes.size.toLong()
                compressedSize = bytes.size.toLong()
                this.crc = crc.value
            }
            zip.putNextEntry(entry)
            zip.write(bytes)
            zip.closeEntry()
        } else {
            zip.putNextEntry(ZipEntry(entryName))
            file.inputStream().use { input ->
                input.copyTo(zip)
            }
            zip.closeEntry()
        }
    }

    private companion object {
        const val APK_RESOURCES_ARSC = "resources.arsc"
    }
}
