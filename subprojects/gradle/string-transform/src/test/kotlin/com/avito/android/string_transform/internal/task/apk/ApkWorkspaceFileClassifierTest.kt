package com.avito.android.string_transform.internal.task.apk

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ApkWorkspaceFileClassifierTest {

    private val classifier = ApkWorkspaceFileClassifier()

    @Test
    fun `workspace file classifier - identifies resources arsc - when file is at apk root`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("resources.arsc").apply {
            writeText("arsc")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.RESOURCES_ARSC)
    }

    @Test
    fun `workspace file classifier - identifies android manifest binary axml - when file is at apk root`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("AndroidManifest.xml").apply {
            writeText("manifest")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.BINARY_AXML)
    }

    @Test
    fun `workspace file classifier - identifies binary axml - when file is non raw resource xml`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("res/values/strings.xml").apply {
            parentFile.mkdirs()
            writeBytes(BINARY_AXML_MAGIC + ByteArray(8))
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.BINARY_AXML)
    }

    @Test
    fun `workspace file classifier - identifies residual - when flat res xml lacks axml magic`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("res/qh.xml").apply {
            parentFile.mkdirs()
            writeText("samplevalue")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }

    @Test
    fun `workspace file classifier - identifies binary axml - when flat res xml has axml magic`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("res/qh.xml").apply {
            parentFile.mkdirs()
            writeBytes(BINARY_AXML_MAGIC + ByteArray(8))
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.BINARY_AXML)
    }

    @Test
    fun `workspace file classifier - identifies raw json as residual - when file is under raw resources`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("res/raw/config.json").apply {
            parentFile.mkdirs()
            writeText("raw")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }

    @Test
    fun `workspace file classifier - identifies raw text as residual - when file is under raw qualifier resources`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("res/raw-en/text.txt").apply {
            parentFile.mkdirs()
            writeText("raw")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }

    @Test
    fun `workspace file classifier - identifies primary dex - when file is classes dex`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("classes.dex").apply {
            writeText("dex")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.DEX)
    }

    @Test
    fun `workspace file classifier - identifies multidex - when file is classes2 dex`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("classes2.dex").apply {
            writeText("dex")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.DEX)
    }

    @Test
    fun `workspace file classifier - identifies kotlin module - when file lives under meta inf`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("META-INF/_sample_module.kotlin_module").apply {
            parentFile.mkdirs()
            writeText("kotlin-module")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.KOTLIN_MODULE)
    }

    @Test
    fun `workspace file classifier - identifies metadata - when file is signature inside meta inf`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("META-INF/CERT.RSA").apply {
            parentFile.mkdirs()
            writeText("signature")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.METADATA)
    }

    @Test
    fun `workspace file classifier - identifies metadata - when file is manifest inside meta inf`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("META-INF/MANIFEST.MF").apply {
            parentFile.mkdirs()
            writeText("manifest")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.METADATA)
    }

    @Test
    fun `workspace file classifier - identifies residual - when file is service loader config inside meta inf`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("META-INF/services/com.example.MyService").apply {
            parentFile.mkdirs()
            writeText("com.example.MyServiceImpl")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }

    @Test
    fun `workspace file classifier - identifies residual - when file is properties payload inside meta inf`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("META-INF/version.properties").apply {
            parentFile.mkdirs()
            writeText("name=foo")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }

    @Test
    fun `workspace file classifier - identifies unsupported binary - when file has pb extension`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("BundleConfig.pb").apply {
            writeText("config")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.UNSUPPORTED_BINARY)
    }

    @Test
    fun `workspace file classifier - identifies residual - when file is plain asset`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("assets/foo.txt").apply {
            parentFile.mkdirs()
            writeText("asset")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }

    @Test
    fun `workspace file classifier - identifies residual - when file is native library`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply { mkdirs() }
        val file = workspace.resolve("lib/arm64-v8a/libfoo.so").apply {
            parentFile.mkdirs()
            writeText("native")
        }

        assertThat(classifier.classify(workspace, file))
            .isEqualTo(ApkWorkspaceFileClassifier.ArtifactClass.RESIDUAL)
    }

    private companion object {
        private val BINARY_AXML_MAGIC = byteArrayOf(0x03, 0x00)
    }
}
