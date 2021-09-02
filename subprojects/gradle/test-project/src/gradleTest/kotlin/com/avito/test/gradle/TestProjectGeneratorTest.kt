package com.avito.test.gradle

import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.FolderModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.module.ParentGradleModule
import com.avito.test.gradle.module.PlatformModule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TestProjectGeneratorTest {

    @Test
    fun `settings include generated`(@TempDir projectDir: File) {
        TestProjectGenerator(modules = listOf(AndroidAppModule("app"))).generateIn(projectDir)

        assertThat(projectDir.file("settings.gradle").readLines()).contains("""include(":app")""")
    }

    @Test
    fun `settings include generated for inner module`(@TempDir projectDir: File) {
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
            """include(":one")""",
            """include(":one:two")"""
        )
    }

    @Test
    fun `settings include generated for inner inner module`(@TempDir projectDir: File) {
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
            """include(":one")""",
            """include(":one:two")""",
            """include(":one:two:three")"""
        )
    }

    @Test
    fun `generating test project - is successful - default values`(@TempDir projectDir: File) {
        TestProjectGenerator().generateIn(projectDir)
        gradlew(
            projectDir,
            "help",
            useModuleClasspath = false
        ).assertThat()
            .buildSuccessful()
    }

    @Test
    fun `generating test project - is successful - no modules`(@TempDir projectDir: File) {
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
    fun `generating test project - is successful - with all module types in groovy`(@TempDir projectDir: File) {
        val androidApp = AndroidAppModule(
            "app",
            dependencies = setOf(
                project(":parent:empty:library"),
                project(":parent:empty:kotlin"),
                project(":parent:empty:platform")
            )
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

    @Test
    fun `generating test project - is successful - with all module types in kotlin script`(@TempDir projectDir: File) {
        val androidApp = AndroidAppModule(
            name = "app",
            dependencies = setOf(
                project(":parent:empty:library"),
                project(":parent:empty:kotlin"),
                project(":parent:empty:platform")
            ),
            useKts = true
        )
        val androidLibrary = AndroidLibModule("library", useKts = true)
        val platform = PlatformModule("platform", useKts = true)
        val kotlin = KotlinModule("kotlin", useKts = true)
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
            modules = listOf(empty),
            useKts = true
        )

        TestProjectGenerator(
            modules = listOf(
                parent
            ),
            useKts = true
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            "assembleDebug",
            useModuleClasspath = false
        ).assertThat()
            .buildSuccessful()
    }
}
