package com.avito.android.string_transform.internal.task.aab

import com.avito.android.Result
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

internal class AabBundleArchiver {

    fun unpackToWorkspace(inputAab: File, workspaceDirectory: File): Result<Unit> = Result.tryCatch {
        workspaceDirectory.mkdirs()

        ZipInputStream(FileInputStream(inputAab)).use { zip ->
            generateSequence { zip.nextEntry }
                .forEach { entry ->
                    val output = workspaceDirectory.resolve(entry.name).canonicalFile
                    check(output.toPath().startsWith(workspaceDirectory.canonicalFile.toPath())) {
                        "Bundle entry resolves outside workspace: ${entry.name}"
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

    fun packFromWorkspace(workspaceDirectory: File, outputAab: File): Result<Unit> = Result.tryCatch {
        outputAab.parentFile.mkdirs()

        ZipOutputStream(FileOutputStream(outputAab)).use { zip ->
            workspaceDirectory.walkTopDown()
                .filter(File::isFile)
                .sortedBy { it.relativeTo(workspaceDirectory).invariantSeparatorsPath }
                .forEach { file ->
                    val entryName = file.relativeTo(workspaceDirectory).invariantSeparatorsPath
                    zip.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { input ->
                        input.copyTo(zip)
                    }
                    zip.closeEntry()
                }
        }
    }
}
