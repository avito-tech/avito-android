package com.avito.android.string_transform.internal.task.apk

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class BinaryAxmlTransformer {

    fun transform(
        inputFile: File,
        rules: List<NormalizedRule>,
    ): Result<Unit> = Result.tryCatch {
        val bytes = inputFile.readBytes()
        val rewritten = rewrite(bytes, rules, inputFile) ?: return@tryCatch
        inputFile.writeBytes(rewritten)
    }

    private fun rewrite(bytes: ByteArray, rules: List<NormalizedRule>, inputFile: File): ByteArray? {
        require(bytes.size >= XML_HEADER_MIN_SIZE) {
            "Binary AXML is too small to contain a ResXMLTree_header (${bytes.size} bytes): ${inputFile.path}"
        }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val xmlType = buffer.getShort(0).toInt() and 0xFFFF
        require(xmlType == TYPE_RES_XML) {
            "Expected top-level ResXMLTree chunk type 0x%04X, was 0x%04X in %s".format(
                TYPE_RES_XML, xmlType, inputFile.path
            )
        }
        val xmlHeaderSize = buffer.getShort(2).toInt() and 0xFFFF
        val xmlChunkSize = buffer.getInt(4)
        require(xmlChunkSize == bytes.size) {
            "ResXMLTree chunk size $xmlChunkSize does not match file length ${bytes.size}"
        }

        val poolOffset = xmlHeaderSize
        val parsed = StringPoolCodec.parse(bytes, chunkOffset = poolOffset)

        val rewrittenStrings = parsed.pool.strings.map { applyRules(it, rules) }
        if (rewrittenStrings == parsed.pool.strings) {
            return null
        }

        val newPoolBytes = StringPoolCodec.serialize(parsed.pool.copy(strings = rewrittenStrings))

        val prefixLength = poolOffset
        val suffixOffset = poolOffset + parsed.chunkSize
        val suffixLength = bytes.size - suffixOffset
        val newTotalSize = prefixLength + newPoolBytes.size + suffixLength

        val output = ByteArray(newTotalSize)
        System.arraycopy(bytes, 0, output, 0, prefixLength)
        System.arraycopy(newPoolBytes, 0, output, prefixLength, newPoolBytes.size)
        if (suffixLength > 0) {
            System.arraycopy(bytes, suffixOffset, output, prefixLength + newPoolBytes.size, suffixLength)
        }

        ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN).putInt(4, newTotalSize)

        return output
    }

    private fun applyRules(value: String, rules: List<NormalizedRule>): String {
        return rules.fold(value) { current, rule -> current.replace(rule.from, rule.to) }
    }

    private companion object {
        const val TYPE_RES_XML: Int = 0x0003
        const val XML_HEADER_MIN_SIZE: Int = 8
    }
}
