package com.avito.android.string_transform.internal.task.aab

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class BundleMetadataCleanerTest {

    private val cleaner = BundleMetadataCleaner()

    @Test
    fun `bundle metadata cleaner - removes stale signing files and preserves supported metadata`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply {
            resolve("META-INF/services").mkdirs()
            resolve("META-INF/MANIFEST.MF").writeText("manifest")
            resolve("META-INF/BNDLTOOL.SF").writeText("signature")
            resolve("META-INF/CHANNEL.RSA").writeText("certificate")
            resolve("META-INF/services/demo.Service").writeText("implementation")
            resolve("META-INF/custom.properties").writeText("payload")
            resolve("base/assets").mkdirs()
            resolve("base/assets/payload.txt").writeText("payload")
        }

        cleaner.clean(workspace).getOrThrow()

        assertThat(workspace.resolve("META-INF/MANIFEST.MF").exists()).isFalse()
        assertThat(workspace.resolve("META-INF/BNDLTOOL.SF").exists()).isFalse()
        assertThat(workspace.resolve("META-INF/CHANNEL.RSA").exists()).isFalse()
        assertThat(workspace.resolve("META-INF/services/demo.Service").readText())
            .isEqualTo("implementation")
        assertThat(workspace.resolve("META-INF/custom.properties").readText()).isEqualTo("payload")
        assertThat(workspace.resolve("base/assets/payload.txt").readText()).isEqualTo("payload")
    }

    @Test
    fun `bundle metadata cleaner - removes meta inf directory - when it becomes empty after cleanup`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply {
            resolve("META-INF").mkdirs()
            resolve("META-INF/BNDLTOOL.SF").writeText("signature")
        }

        cleaner.clean(workspace).getOrThrow()

        assertThat(workspace.resolve("META-INF").exists()).isFalse()
    }
}
