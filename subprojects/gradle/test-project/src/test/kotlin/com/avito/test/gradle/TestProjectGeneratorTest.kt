package com.avito.test.gradle

import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.FolderModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.module.ParentGradleModule
import com.avito.test.gradle.module.PlatformModule
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
                FolderModule(
                    "one",
                    modules = listOf(
                        FolderModule(
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
                FolderModule(
                    "one",
                    modules = listOf(
                        FolderModule(
                            "two",
                            modules = listOf(
                                FolderModule(
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

    @Test
    fun `generating default test project is successful`() {
        TestProjectGenerator().generateIn(projectDir)
        gradlew(
            projectDir,
            "help",
            useModuleClasspath = false
        ).assertThat()
            .buildSuccessful()
    }

    @Test
    fun `generating empty test project is successful`() {
        TestProjectGenerator(
            modules = emptyList()
        ).generateIn(projectDir)
        gradlew(
            projectDir,
            "help",
            useModuleClasspath = false
        ).assertThat()
            .buildSuccessful()
    }

    @Test
    fun `generating test project with all module types is successful`() {
        val androidApp = AndroidAppModule(
            "app",
            dependencies = """
            implementation project(':parent:empty:library')
            implementation project(':parent:empty:kotlin')
            implementation project(':parent:empty:platform')
        """.trimIndent()
        )
        val androidLibrary = AndroidLibModule("library")
        val platform = PlatformModule("platform")
        val kotlin = KotlinModule("kotlin")
        val empty = FolderModule(
            name = "empty",
            modules = listOf(
                androidApp,
                androidLibrary,
                kotlin,
                platform
            )
        )
        val parent = ParentGradleModule(
            name = "parent",
            modules = listOf(empty)
        )
        TestProjectGenerator(
            modules = listOf(
                parent
            )
        ).generateIn(projectDir)
        gradlew(
            projectDir,
            "assembleDebug",
            useModuleClasspath = false
        ).assertThat()
            .buildSuccessful()
    }
}
