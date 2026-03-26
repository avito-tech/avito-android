package com.avito.android.string_transform.internal.task.apk

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ApkOutputPublisherTest {

    private val publisher = ApkOutputPublisher()

    @Test
    fun `publish - copies rebuilt apk to output location - when output parent directory is writable`(
        @TempDir dir: File,
    ) {
        val sourceApk = preparedSourceApk(dir)
        val outputApk = dir.resolve("outputs/transformed-unsigned.apk")

        publisher.publish(sourceApk, outputApk).getOrThrow()

        assertThat(outputApk.readText()).isEqualTo("unsigned apk")
    }

    @Test
    fun `publish - fails - when output parent path is a file`(@TempDir dir: File) {
        val sourceApk = preparedSourceApk(dir)
        val blockedParent = blockedOutputParent(dir)
        val outputApk = blockedParent.resolve("transformed-unsigned.apk")

        val error = assertThrows(Exception::class.java) {
            publisher.publish(sourceApk, outputApk).getOrThrow()
        }

        assertThat(error.message).isNotEmpty()
    }

    private fun preparedSourceApk(dir: File): File {
        return dir.resolve("rebuilt-unsigned.apk").also { sourceApk ->
            sourceApk.writeText("unsigned apk")
        }
    }

    private fun blockedOutputParent(dir: File): File {
        return dir.resolve("outputs").also { blockedParent ->
            blockedParent.writeText("not a directory")
        }
    }
}
