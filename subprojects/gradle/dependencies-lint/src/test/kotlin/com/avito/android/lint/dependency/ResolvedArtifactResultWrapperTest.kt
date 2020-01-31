package com.avito.android.lint.dependency

import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.google.common.truth.Truth.assertThat
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedVariantResult
import org.gradle.api.component.Artifact
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ResolvedArtifactResultWrapperTest {

    private lateinit var testDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        testDir = tempDir.toFile()
    }

    @Test
    fun `use original file - if available`() {
        val fullJar = testDir.file("build/intermediates/full_jar/release/createFullJarRelease/full.jar")

        val files = artifact().files

        assertThat(files).contains(fullJar)
        assertThat(files).hasSize(1)
    }

    @Test
    fun `use intermediate compile classes - if full jar is not available`() {
        testDir.dir("build/intermediates/full_jar/")
        val debugClassesJar = testDir.file("build/intermediates/compile_library_classes/debug/classes.jar")
        val releaseClassesJar = testDir.file("build/intermediates/compile_library_classes/release/classes.jar")

        val file = artifact().files

        assertThat(file).contains(debugClassesJar)
        assertThat(file).contains(releaseClassesJar)
        assertThat(file).hasSize(2)
    }

    private fun artifact(): ResolvedArtifactResultWrapper {
        val fullJar = File(testDir, "build/intermediates/full_jar/release/createFullJarRelease/full.jar")

        val delegate = object : ResolvedArtifactResult {
            override fun getFile(): File {
                return fullJar
            }

            override fun getId(): ComponentArtifactIdentifier {
                TODO("not implemented")
            }

            override fun getType(): Class<out Artifact> {
                TODO("not implemented")
            }

            override fun getVariant(): ResolvedVariantResult {
                TODO("not implemented")
            }
        }
        return ResolvedArtifactResultWrapper(delegate)
    }

}
