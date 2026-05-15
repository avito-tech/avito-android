package com.avito.android.string_transform.internal.task.apk

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal data class StringPool(
    val strings: List<String>,
    val styles: List<Style>,
    val flags: Int,
    val sortedFlag: Boolean,
) {
    val isUtf8: Boolean get() = flags and StringPoolCodec.FLAG_UTF8 != 0
}

internal data class Style(val rawBytes: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Style) return false
        return rawBytes.contentEquals(other.rawBytes)
    }

    override fun hashCode(): Int = rawBytes.contentHashCode()
}

internal data class ParsedStringPool(val pool: StringPool, val chunkSize: Int)

internal object StringPoolCodec {

    const val TYPE_STRING_POOL: Int = 0x0001
    const val HEADER_SIZE: Int = 28
    const val FLAG_SORTED: Int = 0x0001
    const val FLAG_UTF8: Int = 0x0100

    fun parse(bytes: ByteArray, chunkOffset: Int): ParsedStringPool {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(chunkOffset)

        val type = buffer.short.toInt() and 0xFFFF
        require(type == TYPE_STRING_POOL) {
            "Expected ResStringPool chunk type 0x%04X at offset %d, was 0x%04X".format(
                TYPE_STRING_POOL, chunkOffset, type
            )
        }
        val headerSize = buffer.short.toInt() and 0xFFFF
        require(headerSize == HEADER_SIZE) {
            "Expected ResStringPool header size $HEADER_SIZE, was $headerSize"
        }
        val chunkSize = buffer.int
        val stringCount = buffer.int
        val styleCount = buffer.int
        val flags = buffer.int
        val stringsStart = buffer.int
        val stylesStart = buffer.int

        val isUtf8 = flags and FLAG_UTF8 != 0
        val sortedFlag = flags and FLAG_SORTED != 0

        val stringOffsets = IntArray(stringCount) { buffer.int }
        val styleOffsets = IntArray(styleCount) { buffer.int }

        val strings = ArrayList<String>(stringCount)
        for (offset in stringOffsets) {
            val absolute = chunkOffset + stringsStart + offset
            strings += if (isUtf8) {
                decodeUtf8String(bytes, absolute)
            } else {
                decodeUtf16String(bytes, absolute)
            }
        }

        val styles = ArrayList<Style>(styleCount)
        if (styleCount > 0) {
            val stylesAreaStart = chunkOffset + stylesStart
            val stylesAreaEnd = chunkOffset + chunkSize
            for (i in 0 until styleCount) {
                val from = stylesAreaStart + styleOffsets[i]
                val to = if (i + 1 < styleCount) {
                    stylesAreaStart + styleOffsets[i + 1]
                } else {
                    stylesAreaEnd
                }
                styles += Style(bytes.copyOfRange(from, to))
            }
        }

        return ParsedStringPool(
            pool = StringPool(
                strings = strings,
                styles = styles,
                flags = flags,
                sortedFlag = sortedFlag,
            ),
            chunkSize = chunkSize,
        )
    }

    fun serialize(pool: StringPool): ByteArray {
        val stringCount = pool.strings.size
        val styleCount = pool.styles.size
        val isUtf8 = pool.isUtf8

        val encodedStrings = pool.strings.map { encodeString(it, isUtf8) }

        val stringOffsets = IntArray(stringCount)
        var runningStringOffset = 0
        for (i in 0 until stringCount) {
            stringOffsets[i] = runningStringOffset
            runningStringOffset += encodedStrings[i].size
        }
        val stringsBlockUnpaddedSize = runningStringOffset
        val stringsBlockPadding = (4 - stringsBlockUnpaddedSize % 4) % 4
        val stringsBlockSize = stringsBlockUnpaddedSize + stringsBlockPadding

        val styleOffsets = IntArray(styleCount)
        var runningStyleOffset = 0
        for (i in 0 until styleCount) {
            styleOffsets[i] = runningStyleOffset
            runningStyleOffset += pool.styles[i].rawBytes.size
        }
        val stylesBlockSize = runningStyleOffset

        val stringsStart = if (stringCount > 0) {
            HEADER_SIZE + 4 * stringCount + 4 * styleCount
        } else {
            0
        }
        val stylesStart = if (styleCount > 0) {
            HEADER_SIZE + 4 * stringCount + 4 * styleCount + stringsBlockSize
        } else {
            0
        }

        val chunkSize = HEADER_SIZE +
            4 * stringCount +
            4 * styleCount +
            stringsBlockSize +
            stylesBlockSize

        val flagsToWrite = if (pool.sortedFlag) {
            pool.flags or FLAG_SORTED
        } else {
            pool.flags and FLAG_SORTED.inv()
        }

        val output = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)
        output.putShort(TYPE_STRING_POOL.toShort())
        output.putShort(HEADER_SIZE.toShort())
        output.putInt(chunkSize)
        output.putInt(stringCount)
        output.putInt(styleCount)
        output.putInt(flagsToWrite)
        output.putInt(stringsStart)
        output.putInt(stylesStart)

        for (offset in stringOffsets) {
            output.putInt(offset)
        }
        for (offset in styleOffsets) {
            output.putInt(offset)
        }
        for (encoded in encodedStrings) {
            output.put(encoded)
        }
        repeat(stringsBlockPadding) {
            output.put(0)
        }
        for (style in pool.styles) {
            output.put(style.rawBytes)
        }

        return output.array()
    }

    private fun decodeUtf16String(bytes: ByteArray, offset: Int): String {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(offset)
        val first = buffer.short.toInt() and 0xFFFF
        val lengthInChars = if (first and 0x8000 != 0) {
            val low = buffer.short.toInt() and 0xFFFF
            first and 0x7FFF shl 16 or low
        } else {
            first
        }
        val charBuffer = CharArray(lengthInChars)
        for (i in 0 until lengthInChars) {
            charBuffer[i] = (buffer.short.toInt() and 0xFFFF).toChar()
        }
        return String(charBuffer)
    }

    private fun decodeUtf8String(bytes: ByteArray, offset: Int): String {
        var cursor = offset
        // UTF-16 char count (1 or 2 bytes) — unused for decoding
        cursor += utf8LengthBytes(bytes, cursor)
        val (byteLength, byteLengthBytes) = readUtf8Length(bytes, cursor)
        cursor += byteLengthBytes
        return decodeCesu8(bytes, cursor, byteLength)
    }

    private fun utf8LengthBytes(bytes: ByteArray, offset: Int): Int {
        val first = bytes[offset].toInt() and 0xFF
        return if (first and 0x80 != 0) 2 else 1
    }

    private fun readUtf8Length(bytes: ByteArray, offset: Int): Pair<Int, Int> {
        val first = bytes[offset].toInt() and 0xFF
        return if (first and 0x80 != 0) {
            val second = bytes[offset + 1].toInt() and 0xFF
            first and 0x7F shl 8 or second to 2
        } else {
            first to 1
        }
    }

    private fun encodeString(value: String, isUtf8: Boolean): ByteArray {
        return if (isUtf8) encodeUtf8String(value) else encodeUtf16String(value)
    }

    private fun encodeUtf16String(value: String): ByteArray {
        val charCount = value.length
        val prefixBytes = if (charCount >= 0x8000) 4 else 2
        val totalBytes = prefixBytes + charCount * 2 + 2
        val buffer = ByteBuffer.allocate(totalBytes).order(ByteOrder.LITTLE_ENDIAN)
        if (charCount >= 0x8000) {
            buffer.putShort((charCount ushr 16 and 0x7FFF or 0x8000).toShort())
            buffer.putShort((charCount and 0xFFFF).toShort())
        } else {
            buffer.putShort(charCount.toShort())
        }
        for (i in 0 until charCount) {
            buffer.putShort(value[i].code.toShort())
        }
        buffer.putShort(0)
        return buffer.array()
    }

    private fun encodeUtf8String(value: String): ByteArray {
        val cesu8Bytes = encodeCesu8(value)
        val charCount = value.length
        val byteCount = cesu8Bytes.size
        val charPrefix = encodeUtf8Length(charCount)
        val bytePrefix = encodeUtf8Length(byteCount)
        val totalBytes = charPrefix.size + bytePrefix.size + byteCount + 1
        val buffer = ByteBuffer.allocate(totalBytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(charPrefix)
        buffer.put(bytePrefix)
        buffer.put(cesu8Bytes)
        buffer.put(0)
        return buffer.array()
    }

    private fun encodeCesu8(value: String): ByteArray {
        val out = ByteArrayOutputStream(value.length)
        for (i in 0 until value.length) {
            val code = value[i].code
            when {
                code <= 0x7F -> out.write(code)
                code <= 0x7FF -> {
                    out.write(0xC0 or (code ushr 6))
                    out.write(0x80 or (code and 0x3F))
                }
                else -> {
                    out.write(0xE0 or (code ushr 12))
                    out.write(0x80 or (code ushr 6 and 0x3F))
                    out.write(0x80 or (code and 0x3F))
                }
            }
        }
        return out.toByteArray()
    }

    private fun decodeCesu8(bytes: ByteArray, offset: Int, byteLength: Int): String {
        val end = offset + byteLength
        val chars = StringBuilder(byteLength)
        var cursor = offset
        while (cursor < end) {
            val b0 = bytes[cursor].toInt() and 0xFF
            when {
                b0 and 0x80 == 0 -> {
                    chars.append(b0.toChar())
                    cursor += 1
                }
                b0 and 0xE0 == 0xC0 -> {
                    val b1 = bytes[cursor + 1].toInt() and 0xFF
                    val code = b0 and 0x1F shl 6 or (b1 and 0x3F)
                    chars.append(code.toChar())
                    cursor += 2
                }
                b0 and 0xF0 == 0xE0 -> {
                    val b1 = bytes[cursor + 1].toInt() and 0xFF
                    val b2 = bytes[cursor + 2].toInt() and 0xFF
                    val code = b0 and 0x0F shl 12 or (b1 and 0x3F shl 6) or (b2 and 0x3F)
                    chars.append(code.toChar())
                    cursor += 3
                }
                b0 and 0xF8 == 0xF0 -> {
                    val b1 = bytes[cursor + 1].toInt() and 0xFF
                    val b2 = bytes[cursor + 2].toInt() and 0xFF
                    val b3 = bytes[cursor + 3].toInt() and 0xFF
                    val codePoint = b0 and 0x07 shl 18 or
                        (b1 and 0x3F shl 12) or
                        (b2 and 0x3F shl 6) or
                        (b3 and 0x3F)
                    val adjusted = codePoint - 0x10000
                    chars.append((0xD800 or (adjusted ushr 10)).toChar())
                    chars.append((0xDC00 or (adjusted and 0x3FF)).toChar())
                    cursor += 4
                }
                else -> error("Invalid UTF-8 leading byte 0x%02X at offset %d".format(b0, cursor))
            }
        }
        return chars.toString()
    }

    private fun encodeUtf8Length(length: Int): ByteArray {
        require(length in 0..0x7FFF) { "UTF-8 string-pool length out of range: $length" }
        return if (length >= 0x80) {
            byteArrayOf(
                (length ushr 8 and 0x7F or 0x80).toByte(),
                (length and 0xFF).toByte(),
            )
        } else {
            byteArrayOf(length.toByte())
        }
    }
}
