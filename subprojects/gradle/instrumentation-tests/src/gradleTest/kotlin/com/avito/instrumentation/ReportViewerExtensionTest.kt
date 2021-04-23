package com.avito.instrumentation

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.Data
import com.avito.report.model.RunId
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.ObjectInputStream
import java.nio.file.Path

internal class ReportViewerExtensionTest {

    private lateinit var projectDir: File
    private lateinit var commit: String

    private val buildType = "buildType"
    private val reportApiUrlStub = "http://report-api-url"
    private val reportViewerUrlStub = "http://report-viewer-url"
    private val prefixStub = "stub"
    private val fileStorageUrlStub = "http://file-storage-url"

    private val withRunIdBlock = """
        reportViewer {
            reportApiUrl = "$reportApiUrlStub"
            reportViewerUrl = "$reportViewerUrlStub"
            reportRunIdPrefix = "$prefixStub"
            fileStorageUrl = "$fileStorageUrlStub"
        }
    """.trimIndent()

    private val withoutRunIdBlock = """
        reportViewer {
            reportApiUrl = "$reportApiUrlStub"
            reportViewerUrl = "$reportViewerUrlStub"
            fileStorageUrl = "$fileStorageUrlStub"
        }
    """.trimIndent()

    @BeforeEach
    fun setup(@TempDir temp: Path) {
        projectDir = temp.toFile()
    }

    @Test
    fun `report viewer extension - prefix - exists`() {
        generateProject(withRunIdBlock)
        runGit()

        val result = runGradle(
            "app:instrumentationDumpConfiguration"
        )
        result.assertThat().buildSuccessful()

        val data: Data = ObjectInputStream(projectDir.file(instrumentationDumpPath).inputStream()).use {
            it.readObject() as Data
        }

        assertThat(data.reportViewer).isNotNull()
        with(data.reportViewer!!) {
            assertThat(reportApiUrl).isEqualTo(reportApiUrlStub)
            assertThat(reportViewerUrl).isEqualTo(reportViewerUrlStub)
            assertThat(reportRunIdPrefix).isEqualTo(prefixStub)
            assertThat(fileStorageUrl).isEqualTo(fileStorageUrlStub)
        }
        assertThat(data.checkPluginLevelInstrumentationParameters()).containsEntry(
            "runId",
            RunId(prefix = prefixStub, commitHash = commit, buildTypeId = "teamcity-$buildType").value()
        )
    }

    @Test
    fun `report viewer extension - prefix - does not exist`() {
        generateProject(withoutRunIdBlock)
        runGit()

        val result = runGradle(
            "app:instrumentationDumpConfiguration"
        )
        result.assertThat().buildSuccessful()

        val data: Data = ObjectInputStream(projectDir.file(instrumentationDumpPath).inputStream()).use {
            it.readObject() as Data
        }

        assertThat(data.reportViewer).isNotNull()
        with(data.reportViewer!!) {
            assertThat(reportApiUrl).isEqualTo(reportApiUrlStub)
            assertThat(reportViewerUrl).isEqualTo(reportViewerUrlStub)
            assertThat(reportRunIdPrefix).isEmpty()
            assertThat(fileStorageUrl).isEqualTo(fileStorageUrlStub)
        }
        assertThat(data.checkPluginLevelInstrumentationParameters()).containsEntry(
            "runId",
            RunId(commitHash = commit, buildTypeId = "teamcity-$buildType").value()
        )
    }

    private fun generateProject(
        reportViewerBlock: String
    ) {
        projectDir.apply {
            TestProjectGenerator(
                modules = listOf(
                    AndroidAppModule(
                        "app",
                        plugins = plugins {
                            id("com.avito.android.instrumentation-tests")
                        },
                        buildGradleExtra = """
                            android {
                                defaultConfig {
                                    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
                                }
                            }
                            
                            instrumentation {
                                output = project.file("outputs").path
                                sentryDsn = "stub"
                                
                                testReport {
                                    $reportViewerBlock
                                }
                            }
                            """.trimIndent()
                    )
                )
            ).generateIn(this)
        }
    }

    private fun runGradle(vararg args: String) =
        ciRun(
            projectDir,
            *args,
            "-PteamcityBuildId=0",
            buildType = "buildType",
            targetBranch = "another"
        )

    private fun runGit() {
        with(projectDir) {
            git("branch develop")
            commit = git("rev-parse HEAD").trim()
        }
    }
}
