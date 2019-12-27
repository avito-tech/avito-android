package com.avito.test.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class TestProjectGeneratorTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()
    }

    @Test
    fun `settings include generated`() {
        TestProjectGenerator(modules = listOf(AndroidAppModule("app"))).generateIn(projectDir)

        assertThat(projectDir.file("settings.gradle").readLines()).contains("include(':app')")
    }

    @Test
    fun `settings include generated for inner module`() {
        TestProjectGenerator(
            modules = listOf(
                EmptyModule(
                    "one",
                    modules = listOf(
                        EmptyModule(
                            "two",
                            modules = emptyList()
                        )
                    )
                )
            )
        )
            .generateIn(projectDir)

        assertThat(projectDir.file("settings.gradle").readLines()).containsAtLeast(
            "include(':one')",
            "include(':one:two')"
        )
    }

    @Test
    fun `settings include generated for inner inner module`() {
        TestProjectGenerator(
            modules = listOf(
                EmptyModule(
                    "one",
                    modules = listOf(
                        EmptyModule(
                            "two",
                            modules = listOf(
                                EmptyModule(
                                    "three",
                                    modules = emptyList()
                                )
                            )
                        )
                    )
                )
            )
        )
            .generateIn(projectDir)

        assertThat(projectDir.file("settings.gradle").readLines()).containsAtLeast(
            "include(':one')",
            "include(':one:two')",
            "include(':one:two:three')"
        )
    }
}
