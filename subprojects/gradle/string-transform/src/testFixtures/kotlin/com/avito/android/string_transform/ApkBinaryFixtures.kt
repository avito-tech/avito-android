package com.avito.android.string_transform

import com.avito.android.string_transform.internal.task.apk.StringPool
import com.avito.android.string_transform.internal.task.apk.StringPoolCodec
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

internal fun createApkFixture(
    entries: Map<String, ByteArray>,
    storedPaths: Set<String> = emptySet(),
): ByteArray {
    val orderedEntries = linkedMapOf<String, ByteArray>()
    val remaining = entries.toMutableMap()

    remaining.remove("AndroidManifest.xml")?.let { orderedEntries["AndroidManifest.xml"] = it }
    remaining.keys
        .filter { it.matches(Regex("classes\\d*\\.dex")) }
        .sorted()
        .forEach { orderedEntries[it] = remaining.remove(it)!! }
    remaining.remove("resources.arsc")?.let { orderedEntries["resources.arsc"] = it }
    orderedEntries.putAll(remaining)

    val tempFile = Files.createTempFile("apk-fixture-", ".apk").toFile()
    return try {
        createZip(archive = tempFile, entries = orderedEntries, storedPaths = storedPaths)
        tempFile.readBytes()
    } finally {
        tempFile.delete()
    }
}

internal fun createArscBytes(
    strings: List<String>,
    packageTypeStrings: List<String> = emptyList(),
    packageKeyStrings: List<String> = emptyList(),
    packageName: String = "com.example.app",
): ByteArray {
    val poolBytes = StringPoolCodec.serialize(
        StringPool(
            strings = strings,
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )
    )
    val packageChunk = if (packageTypeStrings.isEmpty() && packageKeyStrings.isEmpty()) {
        stubResTablePackageChunk()
    } else {
        resTablePackageChunkWithStringPools(
            packageName = packageName,
            typeStrings = packageTypeStrings,
            keyStrings = packageKeyStrings,
        )
    }
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

internal fun createBinaryAxmlBytes(strings: List<String>, rootElement: String): ByteArray {
    val effectiveStrings = if (strings.contains(rootElement)) strings else listOf(rootElement) + strings
    val rootElementIndex = effectiveStrings.indexOf(rootElement)

    val poolBytes = StringPoolCodec.serialize(
        StringPool(
            strings = effectiveStrings,
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )
    )

    val startElement = buildStartElementChunk(nameIndex = rootElementIndex)
    val endElement = buildEndElementChunk(nameIndex = rootElementIndex)

    val totalSize = XML_HEADER_SIZE + poolBytes.size + startElement.size + endElement.size
    val output = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
    output.putShort(TYPE_RES_XML.toShort())
    output.putShort(XML_HEADER_SIZE.toShort())
    output.putInt(totalSize)
    output.put(poolBytes)
    output.put(startElement)
    output.put(endElement)
    return output.array()
}

private fun stubResTablePackageChunk(): ByteArray {
    val chunkSize = 16
    val buffer = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putShort(TYPE_RES_TABLE_PACKAGE.toShort())
    buffer.putShort(chunkSize.toShort())
    buffer.putInt(chunkSize)
    return buffer.array()
}

private fun resTablePackageChunkWithStringPools(
    packageName: String,
    typeStrings: List<String>,
    keyStrings: List<String>,
): ByteArray {
    val typePoolBytes = StringPoolCodec.serialize(
        StringPool(
            strings = typeStrings,
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )
    )
    val keyPoolBytes = StringPoolCodec.serialize(
        StringPool(
            strings = keyStrings,
            styles = emptyList(),
            flags = StringPoolCodec.FLAG_UTF8,
            sortedFlag = false,
        )
    )
    val chunkSize = PACKAGE_HEADER_SIZE + typePoolBytes.size + keyPoolBytes.size
    val buffer = ByteBuffer.allocate(chunkSize).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putShort(TYPE_RES_TABLE_PACKAGE.toShort())
    buffer.putShort(PACKAGE_HEADER_SIZE.toShort())
    buffer.putInt(chunkSize)
    buffer.putInt(PACKAGE_ID)
    val nameBuffer = ByteArray(PACKAGE_NAME_BYTES)
    val nameChars = packageName.take(PACKAGE_NAME_MAX_CHARS).toCharArray()
    ByteBuffer.wrap(nameBuffer).order(ByteOrder.LITTLE_ENDIAN).apply {
        nameChars.forEach { putShort(it.code.toShort()) }
    }
    buffer.put(nameBuffer)
    val typeStringsOffset = PACKAGE_HEADER_SIZE
    val keyStringsOffset = PACKAGE_HEADER_SIZE + typePoolBytes.size
    buffer.putInt(typeStringsOffset)
    buffer.putInt(typeStrings.size) // lastPublicType
    buffer.putInt(keyStringsOffset)
    buffer.putInt(keyStrings.size) // lastPublicKey
    buffer.putInt(0) // typeIdOffset
    buffer.put(typePoolBytes)
    buffer.put(keyPoolBytes)
    return buffer.array()
}

private fun buildStartElementChunk(nameIndex: Int): ByteArray {
    val headerSize = 16
    val totalSize = 36
    val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putShort(TYPE_RES_XML_START_ELEMENT.toShort())
    buffer.putShort(headerSize.toShort())
    buffer.putInt(totalSize)
    buffer.putInt(1) // lineNumber
    buffer.putInt(STRING_POOL_REF_NONE) // comment
    buffer.putInt(STRING_POOL_REF_NONE) // ns
    buffer.putInt(nameIndex)
    buffer.putShort(20) // attributeStart
    buffer.putShort(20) // attributeSize
    buffer.putShort(0) // attributeCount
    buffer.putShort(0) // idIndex
    buffer.putShort(0) // classIndex
    buffer.putShort(0) // styleIndex
    return buffer.array()
}

private fun buildEndElementChunk(nameIndex: Int): ByteArray {
    val headerSize = 16
    val totalSize = 24
    val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putShort(TYPE_RES_XML_END_ELEMENT.toShort())
    buffer.putShort(headerSize.toShort())
    buffer.putInt(totalSize)
    buffer.putInt(1) // lineNumber
    buffer.putInt(STRING_POOL_REF_NONE) // comment
    buffer.putInt(STRING_POOL_REF_NONE) // ns
    buffer.putInt(nameIndex)
    return buffer.array()
}

private const val TYPE_RES_TABLE: Int = 0x0002
private const val TYPE_RES_TABLE_PACKAGE: Int = 0x0200
private const val TYPE_RES_XML: Int = 0x0003
private const val TYPE_RES_XML_START_ELEMENT: Int = 0x0102
private const val TYPE_RES_XML_END_ELEMENT: Int = 0x0103
private const val TABLE_HEADER_SIZE: Int = 12
private const val XML_HEADER_SIZE: Int = 8
private const val STRING_POOL_REF_NONE: Int = -1
private const val PACKAGE_HEADER_SIZE: Int = 288
private const val PACKAGE_NAME_BYTES: Int = 256
private const val PACKAGE_NAME_MAX_CHARS: Int = 127
private const val PACKAGE_ID: Int = 0x7F
