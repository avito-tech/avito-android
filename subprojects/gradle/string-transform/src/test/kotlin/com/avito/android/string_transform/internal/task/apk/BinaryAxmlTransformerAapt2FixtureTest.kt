package com.avito.android.string_transform.internal.task.apk

import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class BinaryAxmlTransformerAapt2FixtureTest {

    private val transformer = BinaryAxmlTransformer()

    @Test
    fun `transform real aapt2 AndroidManifest_xml - applies rule and preserves emoji`(
        @TempDir dir: File,
    ) {
        val manifestBytes = readResource("/fixtures/aapt2/AndroidManifest.xml")
        val inputFile = dir.resolve("AndroidManifest.xml").apply { writeBytes(manifestBytes) }

        transformer.transform(inputFile, listOf(NormalizedRule("Foo", "Bar"))).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val parsedPool = StringPoolCodec.parse(outputBytes, XML_HEADER_SIZE)

        assertThat(parsedPool.pool.strings).contains("Bar Manifest 🎉")
        assertThat(parsedPool.pool.strings).doesNotContain("Foo Manifest 🎉")
        assertThat(parsedPool.pool.strings.any { it.contains(REPLACEMENT_CHAR) }).isFalse()
    }

    private fun readResource(path: String): ByteArray {
        return checkNotNull(javaClass.getResourceAsStream(path)) {
            "Test resource not found: $path"
        }.use { it.readBytes() }
    }

    private companion object {
        const val XML_HEADER_SIZE: Int = 8
        const val REPLACEMENT_CHAR: Char = '�'
    }
}
