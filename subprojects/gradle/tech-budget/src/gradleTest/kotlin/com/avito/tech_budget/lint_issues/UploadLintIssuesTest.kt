package com.avito.tech_budget.lint_issues

import com.avito.tech_budget.utils.dumpInfoExtension
import com.avito.tech_budget.utils.failureResponse
import com.avito.tech_budget.utils.successResponse
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class UploadLintIssuesTest {
    private val mockDispatcher = MockDispatcher(unmockedResponse = successResponse())
    private val mockWebServer = MockWebServerFactory.create().apply { dispatcher = mockDispatcher }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `when no issues - then log information`(@TempDir projectDir: File) {
        generateProject(projectDir, containsIssues = false)
        uploadLintIssues(projectDir).assertThat()
            .buildSuccessful()
            .outputContains("Nothing to upload. No lint issues found")
    }

    @Test
    fun `when tech budget flag disabled - then fail`(@TempDir projectDir: File) {
        generateProject(projectDir, containsIssues = true)

        uploadLintIssues(projectDir, expectFailure = true, techBudgetFlagEnabled = false)
            .assertThat()
            .buildFailed()
    }

    @Test
    fun `when upload succeed - build success`(@TempDir projectDir: File) {
        generateProject(projectDir, containsIssues = true)

        uploadLintIssues(projectDir)
            .assertThat()
            .buildSuccessful()
            .outputContains("Uploading 3 lint issues")
    }

    @Test
    fun `when upload error - build failure`(@TempDir projectDir: File) {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("/dumpLintIssues") },
                response = failureResponse()
            )
        )
        generateProject(projectDir)

        uploadLintIssues(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload lint issues request failed")
    }

    @Test
    fun `when collect lint issues for multiple app modules - then aggregate`(@TempDir projectDir: File) {
        generateProject(projectDir, isSingleModule = false)

        uploadLintIssues(projectDir)
            .assertThat()
            .buildSuccessful()
            .outputContains("Uploading 6 lint issues")
    }

    private fun generateProject(
        @TempDir projectDir: File,
        containsIssues: Boolean = true,
        isSingleModule: Boolean = true,
    ) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.tech-budget")
                id("com.avito.android.code-ownership")
            },
            buildGradleExtra = """
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                }
            """.trimIndent(),
            modules = listOfNotNull(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    buildGradleExtra = """
                        android {
                            lint {
                                ${if (!containsIssues) "disable.add('GradleDependency')" else ""}
                            }
                        }
                    """.trimIndent(),
                ),
                AndroidAppModule(
                    name = "demoapp",
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                        id("com.avito.android.tech-budget")
                    }
                ).takeIf { !isSingleModule },
            )
        ).generateIn(projectDir)
    }

    private fun uploadLintIssues(
        projectDir: File,
        expectFailure: Boolean = false,
        techBudgetFlagEnabled: Boolean = true
    ): TestResult {
        return gradlew(
            projectDir,
            "uploadLintIssues",
            "-Pcom.avito.android.tech-budget.enable=$techBudgetFlagEnabled",
            expectFailure = expectFailure
        )
    }
}
