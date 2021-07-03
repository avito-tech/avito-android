package com.avito.test.gradle

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.SortedMap
import java.util.TreeMap
import java.util.function.Consumer
import java.util.stream.Collectors.joining

/**
 * Из-за ограничения в junit5 пока невозможно использовать Extension на каждый dynamicTest
 * Используйте пока ManualTempFolder.runIn(File.() -> Unit)
 *
 * https://github.com/junit-team/junit5/issues/694
 */
public object ManualTempFolder {

    public fun runIn(block: (File) -> Unit): File {
        val tempFolder = Files.createTempDirectory("junit")
        val file = tempFolder.toFile()
        try {
            block(file)
        } finally {
            CloseablePath(tempFolder).close()
        }
        return file
    }

    /**
     * реализация скопирована из [org.junitpioneer.jupiter.TempDirectory]
     */
    private class CloseablePath(private val dir: Path) {

        fun close() {
            val failures = deleteAllFilesAndDirectories()
            if (!failures.isEmpty()) {
                throw createIOExceptionWithAttachedFailures(failures)
            }
        }

        private fun deleteAllFilesAndDirectories(): SortedMap<Path, IOException> {
            val failures = TreeMap<Path, IOException>()
            Files.walkFileTree(
                dir,
                object : SimpleFileVisitor<Path>() {

                    override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
                        return deleteAndContinue(file)
                    }

                    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                        return deleteAndContinue(dir)
                    }

                    private fun deleteAndContinue(path: Path): FileVisitResult {
                        try {
                            Files.delete(path)
                        } catch (ex: IOException) {
                            failures[path] = ex
                        }

                        return FileVisitResult.CONTINUE
                    }
                }
            )
            return failures
        }

        private fun createIOExceptionWithAttachedFailures(failures: SortedMap<Path, IOException>): IOException {
            val joinedPaths = failures.keys.stream()
                .peek { this.tryToDeleteOnExit(it) }
                .map<Path> { this.relativizeSafely(it) }
                .map<String> { it.toString() }
                .collect(joining(", "))
            val exception = IOException(
                "Failed to delete temp directory " + dir.toAbsolutePath()
                    + ". The following paths could not be deleted (see suppressed exceptions for details): "
                    + joinedPaths
            )
            failures.values.forEach(Consumer<IOException> { exception.addSuppressed(it) })
            return exception
        }

        private fun tryToDeleteOnExit(path: Path) {
            try {
                path.toFile().deleteOnExit()
            } catch (ignore: UnsupportedOperationException) {
            }
        }

        private fun relativizeSafely(path: Path): Path {
            return try {
                dir.relativize(path)
            } catch (e: IllegalArgumentException) {
                path
            }
        }
    }
}
