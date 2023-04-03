package com.avito.tech_budget.warnings

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.dir
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CollectWarningsTest {

    @Test
    fun `compile without applying plugin to root - build failed`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                    }
                )
            )
        ).generateIn(projectDir)

        collectWarnings(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Plugin `com.avito.android.tech-budget` must be applied to the root project")
    }

    @Test
    fun `compile without warnings - no output`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    }
                )
            )
        ).generateIn(projectDir)

        collectWarnings(projectDir)
            .assertThat()
            .buildSuccessful()

        val warningsDir = File(projectDir, "build/warnings")
        assert(warningsDir.list().isNullOrEmpty())
    }

    @Test
    fun `compile with warnings - directory and files generated`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    mutator = {
                        dir("src/main/kotlin/") {
                            kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                        }
                    }
                )
            )
        ).generateIn(projectDir)

        collectWarnings(projectDir).assertThat().buildSuccessful()

        val warningsDir = File(projectDir, "build/warnings")
        assert(warningsDir.exists())

        val warningsFile = File(warningsDir, "app/compileReleaseKotlin.log")
        val warningsProjectFile = File(warningsDir, "app/.project")

        assert(warningsProjectFile.exists())
        assert(warningsFile.exists())
    }

    @Test
    fun `configure extension - correct output generated`(@TempDir projectDir: File) {
        val separator = "***"
        val outputDirectoryName = "differentOutput"
        val compileWarningsTaskName = "compileDebugKotlin"
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            buildGradleExtra = """
                techBudget { 
                    collectWarnings {
                        outputDirectory.set(project.file("$outputDirectoryName"))
                        warningsSeparator.set("$separator")
                        compileWarningsTaskNames.set(["$compileWarningsTaskName"])
                    }
                }
            """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    mutator = {
                        dir("src/main/kotlin/") {
                            kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                        }
                    }
                )
            )
        ).generateIn(projectDir)

        collectWarnings(projectDir)
            .assertThat()
            .buildSuccessful()

        val warningsFile = File(projectDir, "$outputDirectoryName/app/$compileWarningsTaskName.log")

        Truth
            .assertThat(warningsFile.exists())
            .isTrue()

        Truth
            .assertThat(warningsFile.readText())
            .contains(separator)
    }

    @Test
    fun `compile with warnings different modules - modules do not intersect when compiled in parallel `(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    dependencies = setOf(
                        project(
                            path = ":feed",
                            configuration = IMPLEMENTATION
                        )
                    ),
                    mutator = {
                        dir("src/main/kotlin/") {
                            kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                        }
                    }
                ),
                libModule("feed"),
                libModule("profile"),
                libModule("adverts"),
            )
        ).generateIn(projectDir)

        collectWarnings(projectDir).assertThat().buildSuccessful()

        val feedWarningsFile = File(projectDir, "build/warnings/feed/compileReleaseKotlin.log")
        val profileWarningsFile = File(projectDir, "build/warnings/profile/compileReleaseKotlin.log")
        val advertsWarningsFile = File(projectDir, "build/warnings/adverts/compileReleaseKotlin.log")

        assert(feedWarningsFile.exists())
        assert(profileWarningsFile.exists())
        assert(advertsWarningsFile.exists())

        Truth
            .assertThat(feedWarningsFile.readText())
            .doesNotContainMatch("adverts|profile")
        Truth
            .assertThat(profileWarningsFile.readText())
            .doesNotContainMatch("feed|adverts")
        Truth
            .assertThat(advertsWarningsFile.readText())
            .doesNotContainMatch("feed|profile")
    }

    private fun collectWarnings(projectDir: File, expectFailure: Boolean = false) =
        gradlew(
            projectDir,
            "collectWarnings",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    companion object {
        val WARNING_CONTENT =
            """
            package com.app

            @Deprecated("This is legacy")
            class DeprecatedClass

            class AnotherClass {
                init {
                    val deprecated = DeprecatedClass()
                }
            }
        """.trimIndent()

        private fun libModule(name: String) = AndroidLibModule(
            name = name,
            plugins = plugins {
                id("com.avito.android.tech-budget")
                id("com.avito.android.code-ownership")
            },
            mutator = {
                dir("src/main/kotlin/") {
                    kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                }
            }
        )
    }
}
