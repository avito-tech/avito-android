package com.avito.android.string_transform.internal.task.aab

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal class AabBundleArchiverTest {

    private val archiver = AabBundleArchiver()

    @Test
    fun `bundle archiver - unpacks bundle into workspace - when input aab contains files`(@TempDir dir: File) {
        val inputAab = dir.resolve("input.aab")
        createZip(
            archive = inputAab,
            entries = mapOf(
                "base/manifest/AndroidManifest.xml" to "<manifest />",
                "base/dex/classes.dex" to "dex-content",
            ),
        )
        val workspace = dir.resolve("workspace")

        archiver.unpackToWorkspace(inputAab, workspace).getOrThrow()

        assertThat(workspace.resolve("base/manifest/AndroidManifest.xml").readText()).isEqualTo("<manifest />")
        assertThat(workspace.resolve("base/dex/classes.dex").readText()).isEqualTo("dex-content")
    }

    @Test
    fun `bundle archiver - packs workspace into bundle - when workspace contains files`(@TempDir dir: File) {
        val workspace = dir.resolve("workspace").apply {
            resolve("base/manifest").mkdirs()
            resolve("base/dex").mkdirs()
            resolve("base/manifest/AndroidManifest.xml").writeText("<manifest />")
            resolve("base/dex/classes.dex").writeText("dex-content")
        }
        val outputAab = dir.resolve("output/output.aab")

        archiver.packFromWorkspace(workspace, outputAab).getOrThrow()

        ZipFile(outputAab).use { zip ->
            assertThat(zip.readEntry("base/manifest/AndroidManifest.xml")).isEqualTo("<manifest />")
            assertThat(zip.readEntry("base/dex/classes.dex")).isEqualTo("dex-content")
        }
    }

    @Test
    fun `bundle archiver - fails unpack - when bundle entry escapes workspace`(@TempDir dir: File) {
        val inputAab = dir.resolve("input.aab")
        createZip(
            archive = inputAab,
            entries = mapOf("../outside.txt" to "content"),
        )
        val workspace = dir.resolve("workspace")

        val error = assertThrows(IllegalStateException::class.java) {
            archiver.unpackToWorkspace(inputAab, workspace).getOrThrow()
        }

        assertThat(error.message).contains("Bundle entry resolves outside workspace")
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
