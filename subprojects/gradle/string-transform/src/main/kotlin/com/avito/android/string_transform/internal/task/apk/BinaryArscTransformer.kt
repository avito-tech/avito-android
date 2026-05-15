package com.avito.android.string_transform.internal.task.apk

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class BinaryArscTransformer {

    fun transform(
        inputFile: File,
        rules: List<NormalizedRule>,
    ): Result<Unit> = Result.tryCatch {
        val bytes = inputFile.readBytes()
        val rewritten = rewrite(bytes, rules) ?: return@tryCatch
        inputFile.writeBytes(rewritten)
    }

    private fun rewrite(bytes: ByteArray, rules: List<NormalizedRule>): ByteArray? {
        require(bytes.size >= TABLE_HEADER_MIN_SIZE) {
            "resources.arsc is too small to contain a ResTable_header (${bytes.size} bytes)"
        }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val tableType = buffer.getShort(0).toInt() and 0xFFFF
        require(tableType == TYPE_RES_TABLE) {
            "Expected top-level ResTable chunk type 0x%04X, was 0x%04X".format(TYPE_RES_TABLE, tableType)
        }
        val tableHeaderSize = buffer.getShort(2).toInt() and 0xFFFF
        val tableChunkSize = buffer.getInt(4)
        require(tableChunkSize == bytes.size) {
            "ResTable chunk size $tableChunkSize does not match file length ${bytes.size}"
        }

        val poolOffset = tableHeaderSize
        val parsedPool = StringPoolCodec.parse(bytes, chunkOffset = poolOffset)

        val rewrittenTopLevelStrings = parsedPool.pool.strings.map { applyRules(it, rules) }
        val topLevelChanged = rewrittenTopLevelStrings != parsedPool.pool.strings
        val newPoolBytes = if (topLevelChanged) {
            StringPoolCodec.serialize(parsedPool.pool.copy(strings = rewrittenTopLevelStrings))
        } else {
            bytes.copyOfRange(poolOffset, poolOffset + parsedPool.chunkSize)
        }

        val packagesStart = poolOffset + parsedPool.chunkSize
        val rewrittenChunks = mutableListOf<ByteArray>()
        var anyPackageChanged = false
        var cursor = packagesStart
        while (cursor < bytes.size) {
            val chunkType = buffer.getShort(cursor).toInt() and 0xFFFF
            val chunkSize = buffer.getInt(cursor + 4)
            require(chunkSize > 0) {
                "Encountered zero-sized chunk at offset $cursor in resources.arsc"
            }
            val chunkBytes = bytes.copyOfRange(cursor, cursor + chunkSize)
            if (chunkType == TYPE_RES_TABLE_PACKAGE) {
                val (rewrittenChunk, packageChanged) = rewritePackageChunk(chunkBytes, rules)
                rewrittenChunks += rewrittenChunk
                if (packageChanged) anyPackageChanged = true
            } else {
                rewrittenChunks += chunkBytes
            }
            cursor += chunkSize
        }
        require(cursor == bytes.size) {
            "ResTable chunk walk did not consume the entire file (cursor=$cursor, size=${bytes.size})"
        }

        if (!topLevelChanged && !anyPackageChanged) {
            return null
        }

        val rewrittenChunksTotal = rewrittenChunks.sumOf { it.size }
        val newTotalSize = poolOffset + newPoolBytes.size + rewrittenChunksTotal
        val output = ByteArray(newTotalSize)
        System.arraycopy(bytes, 0, output, 0, poolOffset)
        System.arraycopy(newPoolBytes, 0, output, poolOffset, newPoolBytes.size)
        var writeCursor = poolOffset + newPoolBytes.size
        for (chunk in rewrittenChunks) {
            System.arraycopy(chunk, 0, output, writeCursor, chunk.size)
            writeCursor += chunk.size
        }
        ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN).putInt(4, newTotalSize)
        return output
    }

    private fun rewritePackageChunk(
        packageBytes: ByteArray,
        rules: List<NormalizedRule>,
    ): Pair<ByteArray, Boolean> {
        val buffer = ByteBuffer.wrap(packageBytes).order(ByteOrder.LITTLE_ENDIAN)
        val headerSize = buffer.getShort(2).toInt() and 0xFFFF

        // The package header must be large enough to expose the name and the typeStrings/keyStrings offset fields.
        // Older or stub packages with a smaller header are preserved verbatim.
        if (headerSize < PACKAGE_HEADER_MIN_FIELDS_SIZE) {
            return packageBytes to false
        }

        val originalPackageName = readPackageName(packageBytes)
        val rewrittenPackageName = applyRules(originalPackageName, rules)
        val packageNameChanged = rewrittenPackageName != originalPackageName
        if (packageNameChanged) {
            require(rewrittenPackageName.length <= PACKAGE_NAME_MAX_CHARS) {
                "Rewritten ResTable_package name '$rewrittenPackageName' exceeds " +
                    "$PACKAGE_NAME_MAX_CHARS characters and will not fit in the fixed-size name[128] header field"
            }
        }

        val typeStringsOffset = buffer.getInt(TYPE_STRINGS_OFFSET_FIELD)
        val keyStringsOffset = buffer.getInt(KEY_STRINGS_OFFSET_FIELD)

        val typeStringsRewrite = rewriteInnerStringPool(packageBytes, typeStringsOffset, rules)
        val keyStringsRewrite = rewriteInnerStringPool(packageBytes, keyStringsOffset, rules)

        if (!packageNameChanged && !typeStringsRewrite.changed && !keyStringsRewrite.changed) {
            return packageBytes to false
        }

        val expectedTypeStringsStart = headerSize
        val expectedKeyStringsStart = if (typeStringsOffset > 0) {
            typeStringsOffset + typeStringsRewrite.originalSize
        } else {
            headerSize
        }
        require(typeStringsOffset == 0 || typeStringsOffset == expectedTypeStringsStart) {
            "Unsupported ResTable_package layout: typeStrings pool at offset $typeStringsOffset, " +
                "expected $expectedTypeStringsStart"
        }
        require(keyStringsOffset == 0 || keyStringsOffset == expectedKeyStringsStart) {
            "Unsupported ResTable_package layout: keyStrings pool at offset $keyStringsOffset, " +
                "expected $expectedKeyStringsStart"
        }

        val suffixStart = when {
            keyStringsOffset > 0 -> keyStringsOffset + keyStringsRewrite.originalSize
            typeStringsOffset > 0 -> typeStringsOffset + typeStringsRewrite.originalSize
            else -> headerSize
        }
        val suffixLength = packageBytes.size - suffixStart

        val newTypeStringsBytes = typeStringsRewrite.newBytes
        val newKeyStringsBytes = keyStringsRewrite.newBytes
        val newChunkSize = headerSize + newTypeStringsBytes.size + newKeyStringsBytes.size + suffixLength
        val output = ByteArray(newChunkSize)

        System.arraycopy(packageBytes, 0, output, 0, headerSize)
        if (packageNameChanged) {
            writePackageName(output, rewrittenPackageName)
        }
        var writeCursor = headerSize
        if (newTypeStringsBytes.isNotEmpty()) {
            System.arraycopy(newTypeStringsBytes, 0, output, writeCursor, newTypeStringsBytes.size)
            writeCursor += newTypeStringsBytes.size
        }
        if (newKeyStringsBytes.isNotEmpty()) {
            System.arraycopy(newKeyStringsBytes, 0, output, writeCursor, newKeyStringsBytes.size)
            writeCursor += newKeyStringsBytes.size
        }
        if (suffixLength > 0) {
            System.arraycopy(packageBytes, suffixStart, output, writeCursor, suffixLength)
        }

        val outputBuffer = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN)
        outputBuffer.putInt(4, newChunkSize)
        if (keyStringsOffset > 0 && typeStringsRewrite.changed) {
            val sizeDelta = newTypeStringsBytes.size - typeStringsRewrite.originalSize
            outputBuffer.putInt(KEY_STRINGS_OFFSET_FIELD, keyStringsOffset + sizeDelta)
        }

        return output to true
    }

    private fun readPackageName(packageBytes: ByteArray): String {
        val buffer = ByteBuffer.wrap(packageBytes, PACKAGE_NAME_OFFSET, PACKAGE_NAME_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
        val builder = StringBuilder()
        repeat(PACKAGE_NAME_MAX_CHARS + 1) {
            val code = buffer.short.toInt() and 0xFFFF
            if (code == 0) return builder.toString()
            builder.append(code.toChar())
        }
        return builder.toString()
    }

    private fun writePackageName(output: ByteArray, name: String) {
        output.fill(0, PACKAGE_NAME_OFFSET, PACKAGE_NAME_OFFSET + PACKAGE_NAME_BYTES)
        val buffer = ByteBuffer.wrap(output, PACKAGE_NAME_OFFSET, PACKAGE_NAME_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
        name.forEach { buffer.putShort(it.code.toShort()) }
    }

    private fun rewriteInnerStringPool(
        chunkBytes: ByteArray,
        poolOffset: Int,
        rules: List<NormalizedRule>,
    ): InnerPoolRewrite {
        if (poolOffset <= 0) {
            return InnerPoolRewrite(newBytes = ByteArray(0), originalSize = 0, changed = false)
        }
        val parsed = StringPoolCodec.parse(chunkBytes, chunkOffset = poolOffset)
        val rewrittenStrings = parsed.pool.strings.map { applyRules(it, rules) }
        if (rewrittenStrings == parsed.pool.strings) {
            return InnerPoolRewrite(
                newBytes = chunkBytes.copyOfRange(poolOffset, poolOffset + parsed.chunkSize),
                originalSize = parsed.chunkSize,
                changed = false,
            )
        }
        return InnerPoolRewrite(
            newBytes = StringPoolCodec.serialize(parsed.pool.copy(strings = rewrittenStrings)),
            originalSize = parsed.chunkSize,
            changed = true,
        )
    }

    private fun applyRules(value: String, rules: List<NormalizedRule>): String {
        return rules.fold(value) { current, rule -> current.replace(rule.from, rule.to) }
    }

    private class InnerPoolRewrite(
        val newBytes: ByteArray,
        val originalSize: Int,
        val changed: Boolean,
    )

    private companion object {
        const val TYPE_RES_TABLE: Int = 0x0002
        const val TYPE_RES_TABLE_PACKAGE: Int = 0x0200
        const val TABLE_HEADER_MIN_SIZE: Int = 12

        // ResTable_package header layout up through keyStrings + lastPublicKey:
        // type(2) + headerSize(2) + size(4) + id(4) + name[128](256) +
        // typeStrings(4) + lastPublicType(4) + keyStrings(4) + lastPublicKey(4) = 284 bytes.
        const val PACKAGE_HEADER_MIN_FIELDS_SIZE: Int = 284
        const val PACKAGE_NAME_OFFSET: Int = 12
        const val PACKAGE_NAME_BYTES: Int = 256
        const val PACKAGE_NAME_MAX_CHARS: Int = 127
        const val TYPE_STRINGS_OFFSET_FIELD: Int = 268
        const val KEY_STRINGS_OFFSET_FIELD: Int = 276
    }
}
