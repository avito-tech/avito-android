package com.avito.tech_budget.lint_issues

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import groovy.xml.XmlParser
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CollectLintIssuesTest {

    @Test
    fun `when collect lint issues without applying plugin to root - then build failed`(@TempDir projectDir: File) {
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

        collectLintIssues(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Plugin `com.avito.android.tech-budget` must be applied to the root project")
    }

    @Test
    @Disabled
    fun `when collect lint issues - then expect generate report`(@TempDir projectDir: File) {
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
                ),
            )
        ).generateIn(projectDir)

        collectLintIssues(projectDir)

        val reportsDir = File(projectDir, "app/build/reports")
        assertReportDir(reportsDir)

        val fis = File(reportsDir, "lint-results-debug.xml").inputStream()
        with(XmlParser().parse(fis)) {
            assertThat(name()).isEqualTo("issues")
            assertThat(children()).isNotEmpty()
        }
    }

    @Test
    fun `when collect issues for multiple modules - then generate only for app modules`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
                id("com.avito.android.code-ownership")
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
                            path = ":utils",
                            configuration = IMPLEMENTATION
                        )
                    ),
                ),
                AndroidAppModule(
                    name = "demoapp",
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                        id("com.avito.android.tech-budget")
                    }
                ),
                AndroidLibModule(
                    name = "utils",
                ),
            )
        ).generateIn(projectDir)

        collectLintIssues(projectDir)

        val appReportsDir = File(projectDir, "app/build/reports")
        assertReportDir(appReportsDir)

        val demoAppReportsDir = File(projectDir, "demoapp/build/reports")
        assertReportDir(demoAppReportsDir)

        val libraryReportsDir = File(projectDir, "shared/build/reports")
        assertThat(libraryReportsDir.list().orEmpty()).isEmpty()
    }

    @Test
    fun `when lint xml report disabled - then error`(@TempDir projectDir: File) {
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
                    buildGradleExtra = """
                        android {
                            lint {
                                xmlReport = false
                            }
                        }
                    """.trimIndent(),
                ),
            )
        ).generateIn(projectDir)

        collectLintIssues(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("The 'collectLintIssues' task requires lintConfig.xmlReport to be enabled")
    }

    private fun collectLintIssues(projectDir: File, expectFailure: Boolean = false) =
        gradlew(
            projectDir,
            "collectLintIssues",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    private fun assertReportDir(reportDir: File) {
        val fileNames = reportDir.list()?.asList()
        assertThat(fileNames)
            .hasSize(3)
        assertThat(fileNames)
            .containsExactly(
                "lint-results-debug.xml",
                "lint-results-debug.html",
                "lint-results-debug.txt",
            )
    }
}
