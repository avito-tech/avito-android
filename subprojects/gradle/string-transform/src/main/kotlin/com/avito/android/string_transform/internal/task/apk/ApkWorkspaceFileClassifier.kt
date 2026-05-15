package com.avito.android.string_transform.internal.task.apk

import com.avito.android.string_transform.internal.task.common.MetadataCleaner
import java.io.File

internal class ApkWorkspaceFileClassifier {

    internal enum class ArtifactClass {
        RESOURCES_ARSC,
        BINARY_AXML,
        DEX,
        KOTLIN_MODULE,
        METADATA,
        UNSUPPORTED_BINARY,
        RESIDUAL,
    }

    fun classify(workspaceDirectory: File, file: File): ArtifactClass {
        val relativePath = file.relativeTo(workspaceDirectory).invariantSeparatorsPath
        val fileName = relativePath.substringAfterLast('/')

        return when {
            relativePath == "resources.arsc" ->
                ArtifactClass.RESOURCES_ARSC
            relativePath == "AndroidManifest.xml" ->
                ArtifactClass.BINARY_AXML
            relativePath.startsWith("res/") &&
                relativePath.endsWith(".xml") &&
                !relativePath.startsWith("res/raw/") &&
                !relativePath.startsWith("res/raw-") ->
                if (hasBinaryAxmlMagic(file)) ArtifactClass.BINARY_AXML else ArtifactClass.RESIDUAL
            DEX_PATTERN.matches(relativePath) ->
                ArtifactClass.DEX
            fileName.endsWith(".kotlin_module") ->
                ArtifactClass.KOTLIN_MODULE
            relativePath.startsWith("META-INF/") &&
                MetadataCleaner.isMetaInfSignatureFile(relativePath) ->
                ArtifactClass.METADATA
            relativePath.endsWith(".pb") ->
                ArtifactClass.UNSUPPORTED_BINARY
            else ->
                ArtifactClass.RESIDUAL
        }
    }

    private fun hasBinaryAxmlMagic(file: File): Boolean {
        if (file.length() < 2) return false
        val header = ByteArray(2)
        file.inputStream().use { stream ->
            if (stream.read(header, 0, 2) != 2) return false
        }
        return header[0] == BINARY_AXML_MAGIC_BYTE_0 && header[1] == BINARY_AXML_MAGIC_BYTE_1
    }

    private companion object {
        private val DEX_PATTERN = Regex("classes\\d*\\.dex")
        private const val BINARY_AXML_MAGIC_BYTE_0: Byte = 0x03
        private const val BINARY_AXML_MAGIC_BYTE_1: Byte = 0x00
    }
}
