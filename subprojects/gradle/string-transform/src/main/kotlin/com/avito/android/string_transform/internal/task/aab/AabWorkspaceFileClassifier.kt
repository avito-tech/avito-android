package com.avito.android.string_transform.internal.task.aab

import java.io.File

internal class AabWorkspaceFileClassifier {

    internal enum class ArtifactClass {
        RESOURCES_PB,
        PROTOBUF_XML,
        DEX,
        RESIDUAL,
        METADATA,
        UNSUPPORTED_BINARY,
    }

    fun classify(workspaceDirectory: File, file: File): ArtifactClass {
        val relativePath = file.relativeTo(workspaceDirectory).invariantSeparatorsPath
        val pathSegments = relativePath.split('/')
        val fileName = pathSegments.last()

        return when {
            pathSegments.size == 2 && fileName == "resources.pb" ->
                ArtifactClass.RESOURCES_PB
            pathSegments.size == 3 &&
                pathSegments[1] == "manifest" &&
                fileName == "AndroidManifest.xml" ->
                ArtifactClass.PROTOBUF_XML
            relativePath.contains("/res/raw/") && relativePath.endsWith(".xml") ->
                ArtifactClass.RESIDUAL
            relativePath.contains("/res/") && relativePath.endsWith(".xml") ->
                ArtifactClass.PROTOBUF_XML
            relativePath.contains("/dex/") && relativePath.endsWith(".dex") ->
                ArtifactClass.DEX
            relativePath.startsWith("META-INF/") ->
                ArtifactClass.METADATA
            relativePath.endsWith(".pb") ->
                ArtifactClass.UNSUPPORTED_BINARY
            else ->
                ArtifactClass.RESIDUAL
        }
    }
}
