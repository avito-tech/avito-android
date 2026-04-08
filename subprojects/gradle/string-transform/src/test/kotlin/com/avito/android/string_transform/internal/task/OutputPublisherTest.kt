package com.avito.android.string_transform.internal.task

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.NoSuchFileException

internal class OutputPublisherTest {

    private val publisher = OutputPublisher()

    @Test
    fun `publish - copies source file to output path - when output parent directory is writable`(
        @TempDir dir: File,
    ) {
        val sourceFile = dir.resolve("local-state/source.bin").apply {
            parentFile.mkdirs()
            writeText("artifact content")
        }
        val outputFile = dir.resolve("outputs/published.bin")

        publisher.publish(sourceFile, outputFile).getOrThrow()

        assertThat(outputFile.readText()).isEqualTo("artifact content")
    }

    @Test
    fun `publish - fails - when output parent path is a file`(@TempDir dir: File) {
        val sourceFile = dir.resolve("source.bin").apply {
            writeText("artifact content")
        }
        val blockedParent = dir.resolve("outputs").apply {
            writeText("not a directory")
        }
        val outputFile = blockedParent.resolve("published.bin")

        val error = assertThrows(Exception::class.java) {
            publisher.publish(sourceFile, outputFile).getOrThrow()
        }

        assertThat(error.message).isNotEmpty()
    }

    @Test
    fun `publish - fails - when source file is missing`(@TempDir dir: File) {
        val sourceFile = dir.resolve("missing.bin")
        val outputFile = dir.resolve("outputs/published.bin")

        assertThrows(NoSuchFileException::class.java) {
            publisher.publish(sourceFile, outputFile).getOrThrow()
        }
    }
}
