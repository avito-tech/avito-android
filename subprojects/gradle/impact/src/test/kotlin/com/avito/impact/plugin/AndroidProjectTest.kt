package com.avito.impact.plugin

import com.avito.impact.util.AndroidManifest
import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AndroidProjectTest {

    private lateinit var tempDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.tempDir = tempDir
    }

    @Test
    fun `android manifest - package`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(name = "app", packageName = "com.app")
            )
        ).generateIn(tempDir)

        val projectDir = File(tempDir, "app")
        val manifest = AndroidManifest(projectDir)

        assertThat(manifest.getPackage()).isEqualTo("com.app")
    }
}
