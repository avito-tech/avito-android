package com.avito.android.string_transform.internal.task.aab

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AabOutputPublisherTest {

    private val publisher = AabOutputPublisher()

    @Test
    fun `aab output publisher - copies rebuilt bundle to output path - when source bundle exists`(@TempDir dir: File) {
        val sourceAab = dir.resolve("local-state/rebuilt-unsigned.aab").apply {
            parentFile.mkdirs()
            writeText("bundle")
        }
        val outputAab = dir.resolve("outputs/transformed-unsigned.aab")

        publisher.publish(sourceAab, outputAab).getOrThrow()

        assertThat(outputAab.readText()).isEqualTo("bundle")
    }

    @Test
    fun `aab output publisher - fails publication - when source bundle is missing`(@TempDir dir: File) {
        val sourceAab = dir.resolve("missing.aab")
        val outputAab = dir.resolve("outputs/transformed-unsigned.aab")

        assertThrows(java.nio.file.NoSuchFileException::class.java) {
            publisher.publish(sourceAab, outputAab).getOrThrow()
        }
    }
}
