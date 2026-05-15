package com.avito.android.string_transform.internal.task.apk

import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class BinaryArscTransformerAapt2FixtureTest {

    private val transformer = BinaryArscTransformer()

    @Test
    fun `transform real aapt2 resources_arsc - applies rule and preserves emoji`(
        @TempDir dir: File,
    ) {
        val arscBytes = readResource("/fixtures/aapt2/resources.arsc")
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(inputFile, listOf(NormalizedRule("Foo", "Bar"))).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val parsedPool = StringPoolCodec.parse(outputBytes, TABLE_HEADER_SIZE)

        assertThat(parsedPool.pool.strings).containsExactly(
            "Bar 🎉",
            "Plain Bar string",
            "Привет, Bar! 😀",
            "🚀 boost",
        )
        assertThat(parsedPool.pool.strings.any { it.contains(REPLACEMENT_CHAR) }).isFalse()
    }

    private fun readResource(path: String): ByteArray {
        return checkNotNull(javaClass.getResourceAsStream(path)) {
            "Test resource not found: $path"
        }.use { it.readBytes() }
    }

    private companion object {
        const val TABLE_HEADER_SIZE: Int = 12
        const val REPLACEMENT_CHAR: Char = '�'
    }
}
