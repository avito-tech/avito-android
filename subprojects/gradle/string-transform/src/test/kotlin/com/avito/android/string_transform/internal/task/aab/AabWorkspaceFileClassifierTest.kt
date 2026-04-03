package com.avito.android.string_transform.internal.task.aab

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AabWorkspaceFileClassifierTest {

    private val classifier = AabWorkspaceFileClassifier()

    @Test
    fun `workspace file classifier - identifies resources pb - when file is module resource table`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("base/resources.pb").apply {
            parentFile.mkdirs()
            writeText("resources")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(AabWorkspaceFileClassifier.ArtifactClass.RESOURCES_PB)
    }

    @Test
    fun `workspace file classifier - identifies manifest protobuf xml - when file is module manifest`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("base/manifest/AndroidManifest.xml").apply {
            parentFile.mkdirs()
            writeText("manifest")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(AabWorkspaceFileClassifier.ArtifactClass.PROTOBUF_XML)
    }

    @Test
    fun `workspace file classifier - identifies compiled xml - when file is non raw resource xml`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("base/res/xml/config.xml").apply {
            parentFile.mkdirs()
            writeText("compiled")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(AabWorkspaceFileClassifier.ArtifactClass.PROTOBUF_XML)
    }

    @Test
    fun `workspace file classifier - identifies raw xml as residual - when file is under raw resources`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("base/res/raw/payload.xml").apply {
            parentFile.mkdirs()
            writeText("raw")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(AabWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }

    @Test
    fun `workspace file classifier - identifies dex files - when file is under dex directory`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("base/dex/classes.dex").apply {
            parentFile.mkdirs()
            writeText("dex")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(AabWorkspaceFileClassifier.ArtifactClass.DEX)
    }

    @Test
    fun `workspace file classifier - identifies metadata files - when file is inside meta inf`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("META-INF/BNDLTOOL.SF").apply {
            parentFile.mkdirs()
            writeText("signature")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(AabWorkspaceFileClassifier.ArtifactClass.METADATA)
    }

    @Test
    fun `workspace file classifier - identifies unsupported protobuf binaries - when file is bundle config`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("BundleConfig.pb").apply {
            writeText("config")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(AabWorkspaceFileClassifier.ArtifactClass.UNSUPPORTED_BINARY)
    }

    @Test
    fun `workspace file classifier - identifies residual files - when file is plain asset`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("base/assets/payload.txt").apply {
            parentFile.mkdirs()
            writeText("asset")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(AabWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }
}
