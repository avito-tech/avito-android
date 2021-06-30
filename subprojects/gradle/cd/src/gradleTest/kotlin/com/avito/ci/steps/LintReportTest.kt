package com.avito.ci.steps

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class LintReportTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
    }

    @Test
    fun `lint check - does not send error report - valid project`() {
        generateProject()

        val buildResult = runBuild()
        buildResult.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:lintRelease", TaskOutcome.SUCCESS)
            .taskWithOutcome(":app:lintReportToChannel", TaskOutcome.SUCCESS)
    }

    private fun runBuild(): TestResult {
        return gradlew(
            projectDir,
            "app:release",
            "-Pci=true",
            "-PbuildNumber=0",
            "-PteamcityBuildType=stubBuildType",
            "-PteamcityUrl=http://stub",
            "-PteamcityBuildId=0"
        )
    }

    @Suppress("MaxLineLength")
    private fun generateProject() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.cd")
                        id("com.avito.android.lint-report")
                    },
                    buildGradleExtra = """
                            lintReport {
                                slackToken.set("stub")
                                slackWorkspace.set("stub")
                                slackChannelToReportLintBugs.set(new com.avito.slack.model.SlackChannel("id", "#channel"))
                            }
                            builds {
                                release {
                                    useImpactAnalysis = false
                                    lint { 
                                       slackChannelForAlerts = new com.avito.slack.model.SlackChannel("id", "#channel")
                                    }
                                    artifacts {
                                        file("lintReportHtml", "${projectDir.canonicalPath}/app/build/reports/lint-results-release.html")
                                        file("lintReportXml", "${projectDir.canonicalPath}/app/build/reports/lint-results-release.xml")
                                    }
                                }
                            }
                        """.trimIndent()
                )
            )
        ).generateIn(projectDir)
    }
}
