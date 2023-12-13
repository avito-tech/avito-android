package com.avito.android.network_contracts.validation

import java.io.File
import java.nio.file.Files

/**
 * Computes the set difference of files between two directories.
 * This function returns a collection of files that exist in the current directory but not in the specified other directory.
 * Files are compared based on their relative paths and content.
 *
 * @param otherDirectory The directory to compare against.
 * @return A collection of files that are unique to the current directory.
 */
internal fun File.subtractFilesFrom(otherDirectory: File): Collection<File> {
    if (this.exists() && !otherDirectory.exists() && this.isDirectory) {
        return walk().filter { it.isFile }.toList()
    }

    if (!this.exists() && otherDirectory.exists() && otherDirectory.isDirectory) {
        return otherDirectory.walk().filter { it.isFile }.toList()
    }

    if (!this.exists() && !otherDirectory.exists()) {
        return emptyList()
    }

    require(this.isDirectory && otherDirectory.isDirectory) {
        "${this.path} or ${otherDirectory.path} is not a directory."
    }

    val currentDirectory = this

    val currentFiles = currentDirectory.allFiles
        .associateBy { it.toRelativeString(currentDirectory) }

    val otherFiles = otherDirectory.allFiles
        .associateBy { it.toRelativeString(otherDirectory) }

    return currentFiles
        .filterNot { (relativePath, file) ->
            val otherFile = otherFiles[relativePath]
                ?: return@filterNot false
            file.contentEquals(otherFile)
        }
        .values
}

internal fun File.contentEquals(otherFile: File): Boolean {
    return Files.mismatch(this.toPath(), otherFile.toPath()) == -1L
}

private val File.allFiles: Sequence<File>
    get() = walk().filter(File::isFile)
