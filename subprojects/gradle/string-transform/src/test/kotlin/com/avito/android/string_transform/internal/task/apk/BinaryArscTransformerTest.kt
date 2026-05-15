package com.avito.android.string_transform.internal.task.apk

import com.avito.android.isFailure
import com.avito.android.string_transform.createArscBytes
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class BinaryArscTransformerTest {

    private val transformer = BinaryArscTransformer()

    @Test
    fun `transform - rewrites string pool and preserves package chunk - when rule matches`(
        @TempDir dir: File,
    ) {
        val packageChunk = stubPackageChunk()
        val arscBytes = buildArsc(
            strings = listOf("samplevalue", "untouched"),
            packageChunk = packageChunk,
        )
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(inputFile, listOf(NormalizedRule("samplevalue", "changedvalue"))).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val parsedPool = StringPoolCodec.parse(outputBytes, TABLE_HEADER_SIZE)
        val newPackageOffset = TABLE_HEADER_SIZE + parsedPool.chunkSize

        assertThat(parsedPool.pool.strings)
            .containsExactly("changedvalue", "untouched")
            .inOrder()

        val rewrittenPackageBytes = outputBytes.copyOfRange(newPackageOffset, outputBytes.size)
        assertThat(rewrittenPackageBytes).isEqualTo(packageChunk)

        val tableSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(tableSize).isEqualTo(outputBytes.size)
    }

    @Test
    fun `transform - rewrites top-level chunk size to match new file length`(
        @TempDir dir: File,
    ) {
        val packageChunk = stubPackageChunk()
        val arscBytes = buildArsc(
            strings = listOf("short"),
            packageChunk = packageChunk,
        )
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule("short", "a-much-longer-replacement-value")),
        ).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val tableSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(tableSize).isEqualTo(outputBytes.size)
        assertThat(outputBytes.size).isGreaterThan(arscBytes.size)
    }

    @Test
    fun `transform - leaves bytes untouched - when no rule matches any string`(
        @TempDir dir: File,
    ) {
        val arscBytes = buildArsc(
            strings = listOf("alpha", "beta"),
            packageChunk = stubPackageChunk(),
        )
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(inputFile, listOf(NormalizedRule("nope", "other"))).getOrThrow()

        assertThat(inputFile.readBytes()).isEqualTo(arscBytes)
    }

    @Test
    fun `transform - shrinks total size - when replacement is shorter`(
        @TempDir dir: File,
    ) {
        val packageChunk = stubPackageChunk()
        val arscBytes = buildArsc(
            strings = listOf("an-initially-long-pool-string"),
            packageChunk = packageChunk,
        )
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule("an-initially-long-pool-string", "x")),
        ).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val tableSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(tableSize).isEqualTo(outputBytes.size)
        assertThat(outputBytes.size).isLessThan(arscBytes.size)

        val parsed = StringPoolCodec.parse(outputBytes, TABLE_HEADER_SIZE)
        assertThat(parsed.pool.strings).containsExactly("x").inOrder()

        val newPackageOffset = TABLE_HEADER_SIZE + parsed.chunkSize
        val rewrittenPackageBytes = outputBytes.copyOfRange(newPackageOffset, outputBytes.size)
        assertThat(rewrittenPackageBytes).isEqualTo(packageChunk)
    }

    @Test
    fun `transform - rewrites package keyStrings pool - when rule matches identifier name`(
        @TempDir dir: File,
    ) {
        val arscBytes = createArscBytes(
            strings = listOf("hello samplevalue"),
            packageTypeStrings = listOf("string", "color"),
            packageKeyStrings = listOf("samplevalue_title", "untouched_name"),
        )
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule("samplevalue", "changedvalue")),
        ).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val tableSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(tableSize).isEqualTo(outputBytes.size)

        val parsedTopPool = StringPoolCodec.parse(outputBytes, TABLE_HEADER_SIZE)
        assertThat(parsedTopPool.pool.strings).containsExactly("hello changedvalue").inOrder()

        val packageOffset = TABLE_HEADER_SIZE + parsedTopPool.chunkSize
        val packageHeaderSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN)
            .getShort(packageOffset + 2).toInt() and 0xFFFF
        val packageChunkSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN)
            .getInt(packageOffset + 4)
        assertThat(packageOffset + packageChunkSize).isEqualTo(outputBytes.size)

        val typeStringsOffset = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN)
            .getInt(packageOffset + TYPE_STRINGS_OFFSET_FIELD)
        val keyStringsOffset = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN)
            .getInt(packageOffset + KEY_STRINGS_OFFSET_FIELD)
        assertThat(typeStringsOffset).isEqualTo(packageHeaderSize)

        val parsedTypePool = StringPoolCodec.parse(outputBytes, packageOffset + typeStringsOffset)
        assertThat(parsedTypePool.pool.strings).containsExactly("string", "color").inOrder()

        val parsedKeyPool = StringPoolCodec.parse(outputBytes, packageOffset + keyStringsOffset)
        assertThat(parsedKeyPool.pool.strings)
            .containsExactly("changedvalue_title", "untouched_name")
            .inOrder()
    }

    @Test
    fun `transform - shifts keyStrings offset - when typeStrings pool grows`(
        @TempDir dir: File,
    ) {
        val arscBytes = createArscBytes(
            strings = emptyList(),
            packageTypeStrings = listOf("short"),
            packageKeyStrings = listOf("untouched_name"),
        )
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule("short", "a-much-longer-type-name")),
        ).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val parsedTopPool = StringPoolCodec.parse(outputBytes, TABLE_HEADER_SIZE)
        val packageOffset = TABLE_HEADER_SIZE + parsedTopPool.chunkSize

        val typeStringsOffset = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN)
            .getInt(packageOffset + TYPE_STRINGS_OFFSET_FIELD)
        val keyStringsOffset = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN)
            .getInt(packageOffset + KEY_STRINGS_OFFSET_FIELD)

        val parsedTypePool = StringPoolCodec.parse(outputBytes, packageOffset + typeStringsOffset)
        assertThat(parsedTypePool.pool.strings).containsExactly("a-much-longer-type-name").inOrder()
        assertThat(keyStringsOffset).isEqualTo(typeStringsOffset + parsedTypePool.chunkSize)

        val parsedKeyPool = StringPoolCodec.parse(outputBytes, packageOffset + keyStringsOffset)
        assertThat(parsedKeyPool.pool.strings).containsExactly("untouched_name").inOrder()
    }

    @Test
    fun `transform - rewrites package name field - when rule matches embedded package literal`(
        @TempDir dir: File,
    ) {
        val arscBytes = createArscBytes(
            strings = emptyList(),
            packageTypeStrings = listOf("string"),
            packageKeyStrings = listOf("name"),
            packageName = "com.example.samplevalue",
        )
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule("samplevalue", "changedvalue")),
        ).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val tableSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(tableSize).isEqualTo(outputBytes.size)

        val parsedTopPool = StringPoolCodec.parse(outputBytes, TABLE_HEADER_SIZE)
        val packageOffset = TABLE_HEADER_SIZE + parsedTopPool.chunkSize
        val nameBuffer = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN)
        val decodedName = StringBuilder().apply {
            repeat(PACKAGE_NAME_MAX_CHARS + 1) { index ->
                val code = nameBuffer.getShort(packageOffset + PACKAGE_NAME_OFFSET + index * 2).toInt() and 0xFFFF
                if (code == 0) return@apply
                append(code.toChar())
            }
        }.toString()
        assertThat(decodedName).isEqualTo("com.example.changedvalue")
    }

    @Test
    fun `transform - leaves package chunk untouched - when rules match neither top-level pool nor package pools`(
        @TempDir dir: File,
    ) {
        val arscBytes = createArscBytes(
            strings = listOf("alpha"),
            packageTypeStrings = listOf("string"),
            packageKeyStrings = listOf("name"),
        )
        val inputFile = dir.resolve("resources.arsc").apply { writeBytes(arscBytes) }

        transformer.transform(inputFile, listOf(NormalizedRule("nope", "x"))).getOrThrow()

        assertThat(inputFile.readBytes()).isEqualTo(arscBytes)
    }

    @Test
    fun `transform - returns failure - when input is not a ResTable chunk`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("resources.arsc").apply {
            writeBytes(ByteArray(64) { 0x55.toByte() })
        }

        val result = transformer.transform(inputFile, listOf(NormalizedRule("a", "b")))

        assertThat(result.isFailure()).isTrue()
    }

    @Test
    fun `transform - returns failure - when input file is missing`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("missing.arsc")

        val result = transformer.transform(inputFile, listOf(NormalizedRule("a", "b")))

        assertThat(result.isFailure()).isTrue()
    }

    private fun buildArsc(strings: List<String>, packageChunk: ByteArray): ByteArray {
        val poolBytes = StringPoolCodec.serialize(
            StringPool(
                strings = strings,
                styles = emptyList(),
                flags = StringPoolCodec.FLAG_UTF8,
                sortedFlag = false,
            )
        )
        val totalSize = TABLE_HEADER_SIZE + poolBytes.size + packageChunk.size
        val output = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
        output.putShort(TYPE_RES_TABLE.toShort())
        output.putShort(TABLE_HEADER_SIZE.toShort())
        output.putInt(totalSize)
        output.putInt(1) // packageCount
        output.put(poolBytes)
        output.put(packageChunk)
        return output.array()
    }

    private fun stubPackageChunk(): ByteArray {
        val chunkSize = 16
        val buffer = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(TYPE_RES_TABLE_PACKAGE.toShort())
        buffer.putShort(chunkSize.toShort()) // headerSize equals chunkSize: header-only stub
        buffer.putInt(chunkSize)
        // Remaining 8 bytes left as zeros to stand in for package fields the transformer must not touch.
        return buffer.array()
    }

    private companion object {
        const val TYPE_RES_TABLE: Int = 0x0002
        const val TYPE_RES_TABLE_PACKAGE: Int = 0x0200
        const val TABLE_HEADER_SIZE: Int = 12
        const val TYPE_STRINGS_OFFSET_FIELD: Int = 268
        const val KEY_STRINGS_OFFSET_FIELD: Int = 276
        const val PACKAGE_NAME_OFFSET: Int = 12
        const val PACKAGE_NAME_MAX_CHARS: Int = 127
    }
}
