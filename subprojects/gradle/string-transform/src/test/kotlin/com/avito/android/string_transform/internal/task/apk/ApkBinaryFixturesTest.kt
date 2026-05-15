package com.avito.android.string_transform.internal.task.apk

import com.avito.android.string_transform.createApkFixture
import com.avito.android.string_transform.createArscBytes
import com.avito.android.string_transform.createBinaryAxmlBytes
import com.avito.android.string_transform.createDexBytes
import com.avito.android.string_transform.createKotlinModuleBytes
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipInputStream

internal class ApkBinaryFixturesTest {

    @Test
    fun `createApkFixture - orders manifest dex arsc entries first`() {
        val apkBytes = createApkFixture(
            entries = linkedMapOf(
                "META-INF/CERT.RSA" to byteArrayOf(0x01),
                "assets/data.txt" to "payload".toByteArray(),
                "resources.arsc" to createArscBytes(listOf("alpha")),
                "classes.dex" to createDexBytes(),
                "AndroidManifest.xml" to createBinaryAxmlBytes(listOf("alpha"), "manifest"),
            ),
        )

        val entryOrder = readZipEntryNames(apkBytes)

        assertThat(entryOrder).containsExactly(
            "AndroidManifest.xml",
            "classes.dex",
            "resources.arsc",
            "META-INF/CERT.RSA",
            "assets/data.txt",
        ).inOrder()
    }

    @Test
    fun `createApkFixture - keeps secondary dex files in numeric order ahead of arsc`() {
        val apkBytes = createApkFixture(
            entries = mapOf(
                "classes2.dex" to createDexBytes(),
                "resources.arsc" to createArscBytes(listOf("alpha")),
                "classes.dex" to createDexBytes(),
                "AndroidManifest.xml" to createBinaryAxmlBytes(listOf("alpha"), "manifest"),
            ),
        )

        val entryOrder = readZipEntryNames(apkBytes)

        assertThat(entryOrder).containsExactly(
            "AndroidManifest.xml",
            "classes.dex",
            "classes2.dex",
            "resources.arsc",
        ).inOrder()
    }

    @Test
    fun `createArscBytes - produces parseable resources arsc with given strings - end-to-end through transformer`(
        @TempDir dir: File,
    ) {
        val arscBytes = createArscBytes(listOf("samplevalue", "untouched"))
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        BinaryArscTransformer()
            .transform(inputFile, listOf(NormalizedRule("samplevalue", "changedvalue")))
            .getOrThrow()

        val rewritten = inputFile.readBytes()
        val parsed = StringPoolCodec.parse(rewritten, chunkOffset = TABLE_HEADER_SIZE)
        assertThat(parsed.pool.strings).containsExactly("changedvalue", "untouched").inOrder()

        val tableSize = ByteBuffer.wrap(rewritten).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(tableSize).isEqualTo(rewritten.size)
    }

    @Test
    fun `createBinaryAxmlBytes - produces parseable axml with given strings - end-to-end through transformer`(
        @TempDir dir: File,
    ) {
        val axmlBytes = createBinaryAxmlBytes(
            strings = listOf("samplevalue", "untouched"),
            rootElement = "manifest",
        )
        val inputFile = dir.resolve("AndroidManifest.xml").apply { writeBytes(axmlBytes) }

        BinaryAxmlTransformer()
            .transform(inputFile, listOf(NormalizedRule("samplevalue", "changedvalue")))
            .getOrThrow()

        val rewritten = inputFile.readBytes()
        val parsed = StringPoolCodec.parse(rewritten, chunkOffset = XML_HEADER_SIZE)
        assertThat(parsed.pool.strings).contains("changedvalue")
        assertThat(parsed.pool.strings).contains("manifest")
        assertThat(parsed.pool.strings).doesNotContain("samplevalue")

        val xmlSize = ByteBuffer.wrap(rewritten).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(xmlSize).isEqualTo(rewritten.size)
    }

    @Test
    fun `createBinaryAxmlBytes - prepends root element to string pool - when caller omits it`() {
        val axmlBytes = createBinaryAxmlBytes(
            strings = listOf("samplevalue"),
            rootElement = "manifest",
        )

        val parsed = StringPoolCodec.parse(axmlBytes, chunkOffset = XML_HEADER_SIZE)
        assertThat(parsed.pool.strings).containsExactly("manifest", "samplevalue").inOrder()
    }

    @Test
    fun `createApkFixture - round-trips kotlin module bytes embedded in apk`() {
        val moduleBytes = createKotlinModuleBytes()
        val apkBytes = createApkFixture(
            entries = mapOf(
                "AndroidManifest.xml" to createBinaryAxmlBytes(listOf("alpha"), "manifest"),
                "META-INF/example.kotlin_module" to moduleBytes,
            ),
        )

        val readBack = readZipEntryBytes(apkBytes, "META-INF/example.kotlin_module")
        assertThat(readBack).isEqualTo(moduleBytes)
    }

    private fun readZipEntryNames(bytes: ByteArray): List<String> {
        val names = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                names += entry.name
                entry = zip.nextEntry
            }
        }
        return names
    }

    private fun readZipEntryBytes(bytes: ByteArray, name: String): ByteArray {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == name) {
                    return zip.readBytes()
                }
                entry = zip.nextEntry
            }
        }
        error("Entry not found: $name")
    }

    private companion object {
        const val TABLE_HEADER_SIZE: Int = 12
        const val XML_HEADER_SIZE: Int = 8
    }
}
