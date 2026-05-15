package com.avito.android.string_transform.internal.task.apk

import com.avito.android.isFailure
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class BinaryAxmlTransformerTest {

    private val transformer = BinaryAxmlTransformer()

    @Test
    fun `transform - rewrites string pool and preserves trailing chunks - when rule matches`(
        @TempDir dir: File,
    ) {
        val trailingChunks = stubTrailingChunks()
        val axmlBytes = buildAxml(
            strings = listOf("originalLabel", "untouched"),
            trailingChunks = trailingChunks,
        )
        val inputFile = dir.resolve("AndroidManifest.xml").apply { writeBytes(axmlBytes) }

        transformer.transform(inputFile, listOf(NormalizedRule("originalLabel", "rewrittenLabel"))).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val parsedPool = StringPoolCodec.parse(outputBytes, XML_HEADER_SIZE)
        val newTrailingOffset = XML_HEADER_SIZE + parsedPool.chunkSize

        assertThat(parsedPool.pool.strings)
            .containsExactly("rewrittenLabel", "untouched")
            .inOrder()

        val rewrittenTrailingBytes = outputBytes.copyOfRange(newTrailingOffset, outputBytes.size)
        assertThat(rewrittenTrailingBytes).isEqualTo(trailingChunks)

        val xmlSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(xmlSize).isEqualTo(outputBytes.size)
    }

    @Test
    fun `transform - rewrites top-level chunk size to match new file length`(
        @TempDir dir: File,
    ) {
        val trailingChunks = stubTrailingChunks()
        val axmlBytes = buildAxml(
            strings = listOf("short"),
            trailingChunks = trailingChunks,
        )
        val inputFile = dir.resolve("AndroidManifest.xml").apply { writeBytes(axmlBytes) }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule("short", "a-much-longer-replacement-value")),
        ).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val xmlSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(xmlSize).isEqualTo(outputBytes.size)
        assertThat(outputBytes.size).isGreaterThan(axmlBytes.size)
    }

    @Test
    fun `transform - leaves bytes untouched - when no rule matches any string`(
        @TempDir dir: File,
    ) {
        val axmlBytes = buildAxml(
            strings = listOf("alpha", "beta"),
            trailingChunks = stubTrailingChunks(),
        )
        val inputFile = dir.resolve("AndroidManifest.xml").apply { writeBytes(axmlBytes) }

        transformer.transform(inputFile, listOf(NormalizedRule("nope", "other"))).getOrThrow()

        assertThat(inputFile.readBytes()).isEqualTo(axmlBytes)
    }

    @Test
    fun `transform - shrinks total size - when replacement is shorter`(
        @TempDir dir: File,
    ) {
        val trailingChunks = stubTrailingChunks()
        val axmlBytes = buildAxml(
            strings = listOf("an-initially-long-pool-string"),
            trailingChunks = trailingChunks,
        )
        val inputFile = dir.resolve("AndroidManifest.xml").apply { writeBytes(axmlBytes) }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule("an-initially-long-pool-string", "x")),
        ).getOrThrow()

        val outputBytes = inputFile.readBytes()
        val xmlSize = ByteBuffer.wrap(outputBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(4)
        assertThat(xmlSize).isEqualTo(outputBytes.size)
        assertThat(outputBytes.size).isLessThan(axmlBytes.size)

        val parsed = StringPoolCodec.parse(outputBytes, XML_HEADER_SIZE)
        assertThat(parsed.pool.strings).containsExactly("x").inOrder()

        val newTrailingOffset = XML_HEADER_SIZE + parsed.chunkSize
        val rewrittenTrailingBytes = outputBytes.copyOfRange(newTrailingOffset, outputBytes.size)
        assertThat(rewrittenTrailingBytes).isEqualTo(trailingChunks)
    }

    @Test
    fun `transform - returns failure - when input is not a ResXMLTree chunk`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("AndroidManifest.xml").apply {
            writeBytes(ByteArray(64) { 0x55.toByte() })
        }

        val result = transformer.transform(inputFile, listOf(NormalizedRule("a", "b")))

        assertThat(result.isFailure()).isTrue()
    }

    @Test
    fun `transform - returns failure - when input file is missing`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("missing.xml")

        val result = transformer.transform(inputFile, listOf(NormalizedRule("a", "b")))

        assertThat(result.isFailure()).isTrue()
    }

    private fun buildAxml(strings: List<String>, trailingChunks: ByteArray): ByteArray {
        val poolBytes = StringPoolCodec.serialize(
            StringPool(
                strings = strings,
                styles = emptyList(),
                flags = StringPoolCodec.FLAG_UTF8,
                sortedFlag = false,
            )
        )
        val totalSize = XML_HEADER_SIZE + poolBytes.size + trailingChunks.size
        val output = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
        output.putShort(TYPE_RES_XML.toShort())
        output.putShort(XML_HEADER_SIZE.toShort())
        output.putInt(totalSize)
        output.put(poolBytes)
        output.put(trailingChunks)
        return output.array()
    }

    // Hand-crafts a minimal sequence of trailing AXML chunks:
    // - ResXMLTree_resourceMap (type=0x0180, header-only stub, no resource ids)
    // - RES_XML_START_ELEMENT_TYPE (type=0x0102, header-only stub bytes after the chunk header)
    // - RES_XML_END_ELEMENT_TYPE (type=0x0103, header-only stub bytes after the chunk header)
    // Concrete field layouts are not required: the transformer must leave these bytes untouched.
    private fun stubTrailingChunks(): ByteArray {
        val resourceMap = stubChunk(type = TYPE_RES_XML_RESOURCE_MAP, payloadSize = 0)
        val startElement = stubChunk(type = TYPE_RES_XML_START_ELEMENT, payloadSize = 16)
        val endElement = stubChunk(type = TYPE_RES_XML_END_ELEMENT, payloadSize = 16)
        return resourceMap + startElement + endElement
    }

    private fun stubChunk(type: Int, payloadSize: Int): ByteArray {
        val headerSize = 8
        val totalSize = headerSize + payloadSize
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(type.toShort())
        buffer.putShort(headerSize.toShort())
        buffer.putInt(totalSize)
        // payload left as zeros — the transformer must pass these bytes through unchanged.
        return buffer.array()
    }

    private companion object {
        const val TYPE_RES_XML: Int = 0x0003
        const val TYPE_RES_XML_RESOURCE_MAP: Int = 0x0180
        const val TYPE_RES_XML_START_ELEMENT: Int = 0x0102
        const val TYPE_RES_XML_END_ELEMENT: Int = 0x0103
        const val XML_HEADER_SIZE: Int = 8
    }
}
