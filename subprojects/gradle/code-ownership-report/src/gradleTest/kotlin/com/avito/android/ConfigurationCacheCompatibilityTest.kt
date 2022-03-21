package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin and file report - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
                id("com.avito.android.code-ownership-report")
            },
            imports = listOf(
                "import com.avito.android.model.StubOwner",
                "import com.avito.android.diff.extractor.SimpleOwnersExtractor",
                "import com.avito.android.diff.report.OwnersDiffReportDestination"
            ),
            buildGradleExtra = """
                codeOwnershipDiffReport { 
                    expectedOwnersExtractor.set(SimpleOwnersExtractor(setOf()))
                    actualOwnersExtractor.set(SimpleOwnersExtractor(setOf(StubOwner)))
                    diffReportDestination.set(OwnersDiffReportDestination.File(project.projectDir))
                } 
            """.trimIndent(),
            useKts = true,
            modules = listOf(AndroidLibModule(name = "lib"))
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    @Test
    fun `configuration with applied plugin and slack report - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
                id("com.avito.android.code-ownership-report")
            },
            imports = listOf(
                "import com.avito.android.model.StubOwner",
                "import com.avito.android.diff.extractor.SimpleOwnersExtractor",
                "import com.avito.android.diff.report.OwnersDiffReportDestination",
                "import com.avito.slack.model.SlackChannel"
            ),
            buildGradleExtra = """
                codeOwnershipDiffReport { 
                    expectedOwnersExtractor.set(SimpleOwnersExtractor(setOf()))
                    actualOwnersExtractor.set(SimpleOwnersExtractor(setOf(StubOwner)))
                    diffReportDestination.set(OwnersDiffReportDestination.Slack(
                        token = "anyToken",
                        workspace = "anyWorkspace",
                        channel = SlackChannel(id = "anyId", name = "#test-alerts"),
                        userName = "Android Ownership Diff Reporter"
                    ))
                } 
            """.trimIndent(),
            useKts = true,
            modules = listOf(AndroidLibModule(name = "lib"))
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(tempDir: File): TestResult {
        return gradlew(
            tempDir,
            "reportCodeOwnershipDiff",
            dryRun = false,
            configurationCache = true,
            useTestFixturesClasspath = true
        )
    }
}
