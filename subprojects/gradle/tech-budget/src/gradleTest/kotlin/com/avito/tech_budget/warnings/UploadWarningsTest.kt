package com.avito.tech_budget.warnings

import com.avito.android.tech_budget.warnings.CompilerIssue
import com.avito.android.utils.FAKE_OWNERSHIP_EXTENSION
import com.avito.tech_budget.utils.dumpInfoExtension
import com.avito.tech_budget.utils.failureResponse
import com.avito.tech_budget.utils.successResponse
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class UploadWarningsTest {

    private val mockDispatcher = MockDispatcher(unmockedResponse = successResponse())
    private val mockWebServer = MockWebServerFactory.create()
        .apply {
            dispatcher = mockDispatcher
        }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `compile without warnings - logs information about empty warnings`(@TempDir projectDir: File) {
        generateProject(projectDir, reports = emptyList())
        uploadWarnings(projectDir).assertThat()
            .buildSuccessful()
            .outputContains("No warnings found")
    }

    @Test
    fun `compile with warnings - plugin disabled - no warnings uploaded`(@TempDir projectDir: File) {
        generateProject(projectDir)

        uploadWarnings(projectDir, collectWarningsEnabled = false, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    fun `compile with warnings - uploadWarnings success`(@TempDir projectDir: File) {
        generateProject(projectDir)

        uploadWarnings(projectDir)
            .assertThat()
            .buildSuccessful()
            .outputContains("Found 2 warnings")
    }

    @Test
    fun `compile with 2 warnings - restrict batch size to 1 - two requests sent`(@TempDir projectDir: File) {
        generateProject(projectDir, restrictBatchSize = true)

        val request = mockDispatcher.captureRequest { path.contains("/dumpDetektIssues") }
        uploadWarnings(projectDir)
            .assertThat()
            .buildSuccessful()

        request.Checks().requestsCaptured(requestsCount = 2)
    }

    @Test
    fun `compile with warnings - upload error - build failure`(@TempDir projectDir: File) {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("/dumpDetektIssues") },
                response = failureResponse()
            )
        )
        generateProject(projectDir)

        uploadWarnings(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload warnings request failed")
    }

    @Test
    fun `configure extension - warnings found`(@TempDir projectDir: File) {
        generateProject(projectDir, reports = createWarnings())

        uploadWarnings(projectDir).apply {

            assertThat()
                .buildSuccessful()
                .outputContains("Found 2 warnings")

            assertThat()
                .tasksShouldBeTriggered(":app:reportTask")
        }
    }

    private fun uploadWarnings(
        projectDir: File,
        expectFailure: Boolean = false,
        collectWarningsEnabled: Boolean = true
    ) =
        gradlew(
            projectDir,
            "uploadWarnings",
            "-Pcom.avito.android.tech-budget.enable=$collectWarningsEnabled",
            expectFailure = expectFailure,

            )

    private fun generateProject(
        projectDir: File,
        reports: List<CompilerIssue> = createWarnings(),
        taskName: String = "reportTask",
        includesOwners: Boolean = true,
        restrictBatchSize: Boolean = false,
    ) = TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.gradle-logger")
            id("com.avito.android.tech-budget")
            id("com.avito.android.tls-configuration")
            id("com.avito.android.code-ownership")
        },
        useKts = true,
        buildGradleExtra = buildGradleExtras(reports, includesOwners, restrictBatchSize, taskName),
        imports = listOf(
            "import com.avito.android.tech_budget.warnings.CompilerIssue",
            "import com.avito.android.tech_budget.warnings.CollectWarningsTask",
        ),
        modules = listOf(
            AndroidAppModule(
                name = "app",
                plugins = plugins {
                    id("com.avito.android.tech-budget")
                    id("com.avito.android.code-ownership")
                },
                useKts = true,
                buildGradleExtra = """
                    
                """.trimIndent(),
                mutator = {
                    dir("build/reports/detekt") {
                        file("report.csv")
                    }
                }
            )
        )
    ).generateIn(projectDir)

    @Language("kotlin")
    private fun buildGradleExtras(
        reports: List<CompilerIssue> = emptyList(),
        includesOwners: Boolean = true,
        restrictBatchSize: Boolean = false,
        taskName: String = "reportTask",
    ): String {
        return """ 
                abstract class ReportTask : DefaultTask(), CollectWarningsTask {
                    
                    @get:OutputFile
                    abstract override val warnings: RegularFileProperty
                }
                subprojects.forEach { subproject -> 
                    subproject.tasks.register("$taskName", ReportTask::class) {
                        warningsReports.from(subproject.file("build/reports/detekt/report.csv"))
                    }
                }
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                    ${collectWarningsExtension(reports, restrictBatchSize, taskName)}
                }
                ${if (includesOwners) FAKE_OWNERSHIP_EXTENSION else ""}
                
            """.trimIndent()
    }

    @Language("kotlin")
    private fun collectWarningsExtension(
        reports: List<CompilerIssue>,
        restrictBatchSize: Boolean,
        taskName: String,
    ): String {
        val warnings = reports.joinToString {
            """CompilerIssue("${it.group}", "${it.rule}", ${it.debt}, "${it.location}", "${it.message}")"""
        }

        return """
                       collectWarnings {
                       issuesFileParser.set { file -> listOf($warnings)}
                       compileWarningsTaskName.set("$taskName")
                       
                       ${
            if (restrictBatchSize) {
                """
                            uploadWarningsBatchSize.set(1)
                            uploadWarningsParallelRequestsCount.set(1)
                        """.trimIndent()
            } else {
                ""
            }
        }
                    }
            """.trimIndent()
    }

    private fun createWarnings() = listOf(
        CompilerIssue("group", "rule1", 10, "location1", "message1"),
        CompilerIssue("group", "rule2", 10, "location2", "message2"),
    )
}
