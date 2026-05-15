package com.avito.android.string_transform.internal.task.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal class BinaryArchiverTest {

    private val archiver = BinaryArchiver()

    @Test
    fun `binary archiver - unpacks archive into workspace - when input archive contains files`(@TempDir dir: File) {
        val inputArchive = dir.resolve("input.aab")
        createZip(
            archive = inputArchive,
            entries = mapOf(
                "base/manifest/AndroidManifest.xml" to "<manifest />",
                "base/dex/classes.dex" to "dex-content",
            ),
        )
        val workspace = dir.resolve("workspace")

        archiver.unpackToWorkspace(inputArchive, workspace).getOrThrow()

        assertThat(workspace.resolve("base/manifest/AndroidManifest.xml").readText()).isEqualTo("<manifest />")
        assertThat(workspace.resolve("base/dex/classes.dex").readText()).isEqualTo("dex-content")
    }

    @Test
    fun `binary archiver - packs workspace into archive - when workspace contains files`(@TempDir dir: File) {
        val workspace = dir.resolve("workspace").apply {
            resolve("base/manifest").mkdirs()
            resolve("base/dex").mkdirs()
            resolve("base/manifest/AndroidManifest.xml").writeText("<manifest />")
            resolve("base/dex/classes.dex").writeText("dex-content")
        }
        val outputArchive = dir.resolve("output/output.aab")

        archiver.packFromWorkspace(workspace, outputArchive).getOrThrow()

        ZipFile(outputArchive).use { zip ->
            assertThat(zip.readEntry("base/manifest/AndroidManifest.xml")).isEqualTo("<manifest />")
            assertThat(zip.readEntry("base/dex/classes.dex")).isEqualTo("dex-content")
        }
    }

    @Test
    fun `binary archiver - stores resources arsc uncompressed - to satisfy Android 11+ install requirement`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply {
            mkdirs()
            resolve("resources.arsc").writeBytes(ByteArray(64) { it.toByte() })
            resolve("classes.dex").writeText("dex-content")
        }
        val outputArchive = dir.resolve("output.apk")

        archiver.packFromWorkspace(workspace, outputArchive).getOrThrow()

        ZipFile(outputArchive).use { zip ->
            val arscEntry = checkNotNull(zip.getEntry("resources.arsc"))
            assertThat(arscEntry.method).isEqualTo(ZipEntry.STORED)
            val dexEntry = checkNotNull(zip.getEntry("classes.dex"))
            assertThat(dexEntry.method).isEqualTo(ZipEntry.DEFLATED)
        }
    }

    @Test
    fun `binary archiver - fails unpack - when archive entry escapes workspace`(@TempDir dir: File) {
        val inputArchive = dir.resolve("input.aab")
        createZip(
            archive = inputArchive,
            entries = mapOf("../outside.txt" to "content"),
        )
        val workspace = dir.resolve("workspace")

        val error = assertThrows(IllegalStateException::class.java) {
            archiver.unpackToWorkspace(inputArchive, workspace).getOrThrow()
        }

        assertThat(error.message).contains("Archive entry resolves outside workspace")
    }

    private fun createZip(archive: File, entries: Map<String, String>) {
        archive.parentFile?.mkdirs()
        ZipOutputStream(FileOutputStream(archive)).use { zip ->
            entries.forEach { (path, content) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
    }

    private fun ZipFile.readEntry(path: String): String {
        val entry = checkNotNull(getEntry(path)) {
            "Zip entry not found: $path"
        }
        return getInputStream(entry).bufferedReader().use { it.readText() }
    }
}
