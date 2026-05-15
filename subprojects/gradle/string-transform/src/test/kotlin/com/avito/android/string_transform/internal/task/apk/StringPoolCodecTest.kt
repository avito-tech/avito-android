package com.avito.android.string_transform.internal.task.apk

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class StringPoolCodecTest {

    @Test
    fun `parse - empty pool round-trips`() {
        val original = StringPool(
            strings = emptyList(),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )

        val bytes = StringPoolCodec.serialize(original)
        val parsed = StringPoolCodec.parse(bytes, chunkOffset = 0)

        assertThat(parsed.pool.strings).isEmpty()
        assertThat(parsed.pool.styles).isEmpty()
        assertThat(parsed.pool.isUtf8).isTrue()
        assertThat(parsed.chunkSize).isEqualTo(bytes.size)
        assertThat(parsed.chunkSize).isEqualTo(StringPoolCodec.HEADER_SIZE)
    }

    @Test
    fun `parse - single ASCII string round-trips - when UTF-8 encoding`() {
        val pool = StringPool(
            strings = listOf("hello"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )

        val parsed = roundTrip(pool)

        assertThat(parsed.pool.strings).containsExactly("hello").inOrder()
        assertThat(parsed.pool.isUtf8).isTrue()
        assertThat(parsed.pool.sortedFlag).isFalse()
    }

    @Test
    fun `parse - single ASCII string round-trips - when UTF-16 encoding`() {
        val pool = StringPool(
            strings = listOf("hello"),
            styles = emptyList(),
            flags = 0,
            sortedFlag = false,
        )

        val parsed = roundTrip(pool)

        assertThat(parsed.pool.strings).containsExactly("hello").inOrder()
        assertThat(parsed.pool.isUtf8).isFalse()
    }

    @Test
    fun `parse - multi-byte UTF-8 string round-trips`() {
        val pool = StringPool(
            strings = listOf("héllo € 中文"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )

        val parsed = roundTrip(pool)

        assertThat(parsed.pool.strings)
            .containsExactly("héllo € 中文")
            .inOrder()
    }

    @Test
    fun `parse - UTF-16 string with surrogate pair round-trips`() {
        val poundOfEmoji = "abc 😀 def" // grinning face U+1F600

        val pool = StringPool(
            strings = listOf(poundOfEmoji),
            styles = emptyList(),
            flags = 0,
            sortedFlag = false,
        )

        val parsed = roundTrip(pool)

        assertThat(parsed.pool.strings).containsExactly(poundOfEmoji).inOrder()
    }

    @Test
    fun `parse - UTF-8 string with surrogate pair round-trips - and reports correct char count`() {
        val emojiString = "x😀y" // grinning face U+1F600

        val pool = StringPool(
            strings = listOf(emojiString),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )

        val parsed = roundTrip(pool)

        assertThat(parsed.pool.strings).containsExactly(emojiString).inOrder()
    }

    @Test
    fun `serialize - UTF-8 string with surrogate pair - encodes supplementary char as CESU-8 surrogate pair`() {
        // AAPT2 emits supplementary-plane chars in ResStringPool UTF-8 as CESU-8:
        // each UTF-16 surrogate half becomes its own 3-byte sequence (no 4-byte sequences).
        // For "x😀y" (U+1F600 → high D83D, low DE00):
        //   charCount = 4 (UTF-16 code units), byteCount = 8 (1 + 3 + 3 + 1)
        val pool = StringPool(
            strings = listOf("x😀y"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )

        val bytes = StringPoolCodec.serialize(pool)
        val stringStart = StringPoolCodec.HEADER_SIZE + 4 // header + 1 offset
        val expected = byteArrayOf(
            0x04, 0x08, // charCount=4, byteCount=8
            0x78.toByte(), // 'x'
            0xED.toByte(), 0xA0.toByte(), 0xBD.toByte(), // high surrogate D83D
            0xED.toByte(), 0xB8.toByte(), 0x80.toByte(), // low surrogate DE00
            0x79.toByte(), // 'y'
            0x00, // NUL terminator
        )
        val actual = bytes.copyOfRange(stringStart, stringStart + expected.size)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `parse - CESU-8 string pool from aapt2-style bytes - decodes supplementary char correctly`() {
        val poolBytes = buildSingleStringUtf8PoolBytes(
            charCount = 4,
            cesu8Bytes = byteArrayOf(
                0x78.toByte(), // 'x'
                0xED.toByte(), 0xA0.toByte(), 0xBD.toByte(), // CESU-8 high surrogate
                0xED.toByte(), 0xB8.toByte(), 0x80.toByte(), // CESU-8 low surrogate
                0x79.toByte(), // 'y'
            ),
        )

        val parsed = StringPoolCodec.parse(poolBytes, chunkOffset = 0)

        assertThat(parsed.pool.strings).containsExactly("x😀y").inOrder()
    }

    @Test
    fun `parse - 4-byte UTF-8 supplementary sequence - decodes to surrogate pair for non-aapt2 inputs`() {
        val poolBytes = buildSingleStringUtf8PoolBytes(
            charCount = 4,
            cesu8Bytes = byteArrayOf(
                0x78.toByte(), // 'x'
                0xF0.toByte(), 0x9F.toByte(), 0x98.toByte(), 0x80.toByte(), // 4-byte UTF-8 U+1F600
                0x79.toByte(), // 'y'
            ),
        )

        val parsed = StringPoolCodec.parse(poolBytes, chunkOffset = 0)

        assertThat(parsed.pool.strings).containsExactly("x😀y").inOrder()
    }

    @Test
    fun `serialize - re-emits CESU-8 byte-for-byte after parse round-trip from aapt2 bytes`() {
        val originalBytes = buildSingleStringUtf8PoolBytes(
            charCount = 4,
            cesu8Bytes = byteArrayOf(
                0x78.toByte(),
                0xED.toByte(), 0xA0.toByte(), 0xBD.toByte(),
                0xED.toByte(), 0xB8.toByte(), 0x80.toByte(),
                0x79.toByte(),
            ),
        )

        val parsed = StringPoolCodec.parse(originalBytes, chunkOffset = 0)
        val reSerialized = StringPoolCodec.serialize(parsed.pool)

        assertThat(reSerialized).isEqualTo(originalBytes)
    }

    @Test
    fun `serialize - growth scenario - chunk size grows when string is replaced with longer value`() {
        val original = StringPool(
            strings = listOf("short"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )
        val originalBytes = StringPoolCodec.serialize(original)

        val expanded = original.copy(strings = listOf("a-much-longer-replacement-string"))
        val expandedBytes = StringPoolCodec.serialize(expanded)

        assertThat(expandedBytes.size).isGreaterThan(originalBytes.size)

        val parsed = StringPoolCodec.parse(expandedBytes, chunkOffset = 0)
        assertThat(parsed.pool.strings)
            .containsExactly("a-much-longer-replacement-string")
            .inOrder()
        assertThat(parsed.chunkSize).isEqualTo(expandedBytes.size)
    }

    @Test
    fun `serialize - shrink scenario - chunk size shrinks when string is replaced with shorter value`() {
        val original = StringPool(
            strings = listOf("an-initially-long-pool-string"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )
        val originalBytes = StringPoolCodec.serialize(original)

        val shrunk = original.copy(strings = listOf("tiny"))
        val shrunkBytes = StringPoolCodec.serialize(shrunk)

        assertThat(shrunkBytes.size).isLessThan(originalBytes.size)

        val parsed = StringPoolCodec.parse(shrunkBytes, chunkOffset = 0)
        assertThat(parsed.pool.strings).containsExactly("tiny").inOrder()
        assertThat(parsed.chunkSize).isEqualTo(shrunkBytes.size)
    }

    @Test
    fun `parse - preserves sorted flag - when set in flags input`() {
        val pool = StringPool(
            strings = listOf("a", "b"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = true,
        )

        val parsed = roundTrip(pool)

        assertThat(parsed.pool.sortedFlag).isTrue()
        assertThat(parsed.pool.isUtf8).isTrue()
    }

    @Test
    fun `serialize - preserves multiple strings in order`() {
        val pool = StringPool(
            strings = listOf("first", "second", "third", "fourth"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )

        val parsed = roundTrip(pool)

        assertThat(parsed.pool.strings)
            .containsExactly("first", "second", "third", "fourth")
            .inOrder()
    }

    @Test
    fun `parse - style data passthrough - preserves raw span bytes verbatim`() {
        val styleSpan = ByteArray(12).also { bytes ->
            // ResStringPool_span: name index=0, firstChar=0, lastChar=4
            bytes[0] = 0; bytes[1] = 0; bytes[2] = 0; bytes[3] = 0
            bytes[4] = 0; bytes[5] = 0; bytes[6] = 0; bytes[7] = 0
            bytes[8] = 4; bytes[9] = 0; bytes[10] = 0; bytes[11] = 0
        }
        // Per-style END marker (4 bytes 0xFF) + global END span (12 bytes of 0xFF)
        val perStyleEnd = ByteArray(4) { 0xFF.toByte() }
        val globalEnd = ByteArray(12) { 0xFF.toByte() }
        val styleBytes = styleSpan + perStyleEnd + globalEnd

        val pool = StringPool(
            strings = listOf("styled-string", "span-name"),
            styles = listOf(Style(styleBytes)),
            flags = 0,
            sortedFlag = false,
        )

        val serialized = StringPoolCodec.serialize(pool)
        val parsed = StringPoolCodec.parse(serialized, chunkOffset = 0)

        assertThat(parsed.pool.strings)
            .containsExactly("styled-string", "span-name")
            .inOrder()
        assertThat(parsed.pool.styles).hasSize(1)
        assertThat(parsed.pool.styles.single().rawBytes).isEqualTo(styleBytes)
    }

    @Test
    fun `parse - reads pool from non-zero chunk offset`() {
        val pool = StringPool(
            strings = listOf("offset-test"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )
        val poolBytes = StringPoolCodec.serialize(pool)
        val padding = ByteArray(16) { 0x00 }
        val combined = padding + poolBytes

        val parsed = StringPoolCodec.parse(combined, chunkOffset = padding.size)

        assertThat(parsed.pool.strings).containsExactly("offset-test").inOrder()
        assertThat(parsed.chunkSize).isEqualTo(poolBytes.size)
    }

    @Test
    fun `parse - UTF-8 string with byte count above 0x7F round-trips - using 2-byte length prefix`() {
        // 200 ASCII bytes triggers the 2-byte length-prefix path in both encodeUtf8Length and
        // readUtf8Length (length >= 0x80 sets the high bit on the first prefix byte).
        val longAscii = "x".repeat(200)

        val pool = StringPool(
            strings = listOf(longAscii),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )

        val parsed = roundTrip(pool)

        assertThat(parsed.pool.strings).containsExactly(longAscii).inOrder()
    }

    @Test
    fun `serialize - pads strings block to 4-byte boundary`() {
        // A 3-character ASCII UTF-8 string: 1-byte char count + 1-byte byte count + 3 bytes + 1 NUL = 6 bytes.
        // Not a multiple of 4, so the strings block must be padded.
        val pool = StringPool(
            strings = listOf("abc"),
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )

        val bytes = StringPoolCodec.serialize(pool)

        assertThat(bytes.size % 4).isEqualTo(0)
        val parsed = StringPoolCodec.parse(bytes, chunkOffset = 0)
        assertThat(parsed.pool.strings).containsExactly("abc").inOrder()
        assertThat(parsed.chunkSize).isEqualTo(bytes.size)
    }

    private fun roundTrip(pool: StringPool): ParsedStringPool {
        val bytes = StringPoolCodec.serialize(pool)
        val parsed = StringPoolCodec.parse(bytes, chunkOffset = 0)
        assertThat(parsed.chunkSize).isEqualTo(bytes.size)
        return parsed
    }

    private fun buildSingleStringUtf8PoolBytes(charCount: Int, cesu8Bytes: ByteArray): ByteArray {
        // ResStringPool: header(28) + 1 offset(4) + 1 string(charPrefix + bytePrefix + cesu8 + NUL).
        val byteCount = cesu8Bytes.size
        require(charCount in 0..0x7F) { "this helper only supports 1-byte char prefix" }
        require(byteCount in 0..0x7F) { "this helper only supports 1-byte byte prefix" }
        val stringEntrySize = 1 + 1 + byteCount + 1
        val unpadded = StringPoolCodec.HEADER_SIZE + 4 + stringEntrySize
        val padding = (4 - unpadded % 4) % 4
        val totalSize = unpadded + padding

        val output = java.nio.ByteBuffer.allocate(totalSize).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        output.putShort(StringPoolCodec.TYPE_STRING_POOL.toShort())
        output.putShort(StringPoolCodec.HEADER_SIZE.toShort())
        output.putInt(totalSize)
        output.putInt(1) // stringCount
        output.putInt(0) // styleCount
        output.putInt(StringPoolCodec.FLAG_UTF8) // flags
        output.putInt(StringPoolCodec.HEADER_SIZE + 4) // stringsStart
        output.putInt(0) // stylesStart
        output.putInt(0) // string offset 0
        output.put(charCount.toByte())
        output.put(byteCount.toByte())
        output.put(cesu8Bytes)
        output.put(0) // NUL terminator
        repeat(padding) { output.put(0) }
        return output.array()
    }
}
