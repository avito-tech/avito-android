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

        val manifest = archiver.unpackToWorkspace(inputArchive, workspace).getOrThrow()

        assertThat(workspace.resolve("base/manifest/AndroidManifest.xml").readText()).isEqualTo("<manifest />")
        assertThat(workspace.resolve("base/dex/classes.dex").readText()).isEqualTo("dex-content")
        assertThat(manifest.methodFor("base/manifest/AndroidManifest.xml")).isEqualTo(ZipEntry.DEFLATED)
        assertThat(manifest.methodFor("base/dex/classes.dex")).isEqualTo(ZipEntry.DEFLATED)
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

        archiver.packFromWorkspace(workspace, outputArchive, CompressionManifest.EMPTY).getOrThrow()

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

        archiver.packFromWorkspace(workspace, outputArchive, CompressionManifest.EMPTY).getOrThrow()

        ZipFile(outputArchive).use { zip ->
            val arscEntry = checkNotNull(zip.getEntry("resources.arsc"))
            assertThat(arscEntry.method).isEqualTo(ZipEntry.STORED)
            val dexEntry = checkNotNull(zip.getEntry("classes.dex"))
            assertThat(dexEntry.method).isEqualTo(ZipEntry.DEFLATED)
        }
    }

    @Test
    fun `binary archiver - preserves STORED method from manifest - during round-trip`(@TempDir dir: File) {
        val inputArchive = dir.resolve("input.apk")
        createZip(
            archive = inputArchive,
            entries = mapOf(
                "lib/arm64-v8a/libnative.so" to "native-lib-bytes",
                "classes.dex" to "dex-content",
            ),
            storedPaths = setOf("lib/arm64-v8a/libnative.so"),
        )
        val workspace = dir.resolve("workspace")
        val manifest = archiver.unpackToWorkspace(inputArchive, workspace).getOrThrow()

        val outputArchive = dir.resolve("output.apk")
        archiver.packFromWorkspace(workspace, outputArchive, manifest).getOrThrow()

        ZipFile(outputArchive).use { zip ->
            val soEntry = checkNotNull(zip.getEntry("lib/arm64-v8a/libnative.so"))
            assertThat(soEntry.method).isEqualTo(ZipEntry.STORED)
            val dexEntry = checkNotNull(zip.getEntry("classes.dex"))
            assertThat(dexEntry.method).isEqualTo(ZipEntry.DEFLATED)
            assertThat(zip.readEntry("lib/arm64-v8a/libnative.so")).isEqualTo("native-lib-bytes")
            assertThat(zip.readEntry("classes.dex")).isEqualTo("dex-content")
        }
    }

    @Test
    fun `binary archiver - forces resources arsc STORED - even when manifest says DEFLATED`(@TempDir dir: File) {
        val workspace = dir.resolve("workspace").apply {
            mkdirs()
            resolve("resources.arsc").writeBytes(ByteArray(64) { it.toByte() })
            resolve("classes.dex").writeText("dex-content")
        }
        val manifest = CompressionManifest(
            mapOf(
                "resources.arsc" to ZipEntry.DEFLATED,
                "classes.dex" to ZipEntry.DEFLATED,
            )
        )
        val outputArchive = dir.resolve("output.apk")

        archiver.packFromWorkspace(workspace, outputArchive, manifest).getOrThrow()

        ZipFile(outputArchive).use { zip ->
            val arscEntry = checkNotNull(zip.getEntry("resources.arsc"))
            assertThat(arscEntry.method).isEqualTo(ZipEntry.STORED)
        }
    }

    @Test
    fun `binary archiver - defaults to DEFLATED - when entry is absent from manifest`(@TempDir dir: File) {
        val workspace = dir.resolve("workspace").apply {
            mkdirs()
            resolve("new-file.txt").writeText("content")
        }
        val outputArchive = dir.resolve("output.apk")

        archiver.packFromWorkspace(workspace, outputArchive, CompressionManifest.EMPTY).getOrThrow()

        ZipFile(outputArchive).use { zip ->
            val entry = checkNotNull(zip.getEntry("new-file.txt"))
            assertThat(entry.method).isEqualTo(ZipEntry.DEFLATED)
        }
    }

    @Test
    fun `binary archiver - captures compression methods - when archive has STORED and DEFLATED entries`(
        @TempDir dir: File,
    ) {
        val inputArchive = dir.resolve("input.apk")
        val storedContent = "native-lib-bytes"
        val deflatedContent = "dex-content"
        createZip(
            archive = inputArchive,
            entries = mapOf(
                "lib/arm64-v8a/libnative.so" to storedContent,
                "classes.dex" to deflatedContent,
            ),
            storedPaths = setOf("lib/arm64-v8a/libnative.so"),
        )
        val workspace = dir.resolve("workspace")

        val manifest = archiver.unpackToWorkspace(inputArchive, workspace).getOrThrow()

        assertThat(manifest.methodFor("lib/arm64-v8a/libnative.so")).isEqualTo(ZipEntry.STORED)
        assertThat(manifest.methodFor("classes.dex")).isEqualTo(ZipEntry.DEFLATED)
        assertThat(workspace.resolve("lib/arm64-v8a/libnative.so").readText()).isEqualTo(storedContent)
        assertThat(workspace.resolve("classes.dex").readText()).isEqualTo(deflatedContent)
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

    private fun createZip(
        archive: File,
        entries: Map<String, String>,
        storedPaths: Set<String> = emptySet(),
    ) {
        archive.parentFile?.mkdirs()
        ZipOutputStream(FileOutputStream(archive)).use { zip ->
            entries.forEach { (path, content) ->
                val bytes = content.toByteArray()
                if (path in storedPaths) {
                    val crc = java.util.zip.CRC32().apply { update(bytes) }
                    val entry = ZipEntry(path).apply {
                        method = ZipEntry.STORED
                        size = bytes.size.toLong()
                        compressedSize = bytes.size.toLong()
                        this.crc = crc.value
                    }
                    zip.putNextEntry(entry)
                    zip.write(bytes)
                    zip.closeEntry()
                } else {
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(bytes)
                    zip.closeEntry()
                }
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
