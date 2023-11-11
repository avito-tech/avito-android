package com.avito.instrumentation

import com.avito.instrumentation.configuration.report.ReportConfig
import com.avito.instrumentation.internal.LocalRunArgsChecker
import com.avito.reportviewer.model.RunId
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.git
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.div

internal class LocalRunArgsTest {

    private val appName = "app"
    private val targetBranch = "another"
    private val buildType = "buildType"

    private val reportConfig = ReportConfig.ReportViewer.SendFromDevice(
        reportApiUrl = "http://stub",
        reportViewerUrl = "http://stub",
        fileStorageUrl = "http://stub",
        planSlug = "AppAndroid",
        jobSlug = "LocalTests",
    )

    @TestFactory
    fun `instrumentation args - passed correctly - kotlin`(@TempDir projectDir: File): List<DynamicTest> {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = appName,
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    useKts = true,
                    imports = listOf(
                        "import com.avito.instrumentation.reservation.request.Device",
                        "import com.avito.instrumentation.configuration.report.ReportConfig",
                    ),
                    buildGradleExtra = kotlinStubConfig(reportConfig)
                )
            ),
            useKts = true,
        ).generateIn(projectDir)

        return cases(projectDir)
    }

    private fun cases(projectDir: File): List<DynamicTest> {
        with(projectDir) {
            git("branch $targetBranch")
        }
        ciRun(
            projectDir,
            "help",
            "-PteamcityBuildId=0",
            "-Pavito.git.state=env",
            "-PisGradleTestKitRun=true",
            buildType = buildType,
        ).assertThat().buildSuccessful()

        val dumpDir = projectDir.toPath() / "outputs" / dumpDirName

        val instrumentationArgs = LocalRunArgsChecker { dumpDir.toFile() }.readDump()
        val commit = projectDir.git("rev-parse HEAD").trim()
        val runId = RunId(
            identifier = commit,
            buildTypeId = "teamcity-$buildType"
        ).toReportViewerFormat()

        val cases = listOf(
            Case("planSlug", "AppAndroid"),
            Case("jobSlug", "LocalTests"),
            Case("runId", runId),
            Case("avito.report.transport", "backend"),
            Case("fileStorageUrl", "http://stub"),
            Case("deviceName", "local"),
            Case("reportApiUrl", "http://stub"),
            Case("reportViewerUrl", "http://stub"),
            Case("expectedCustomParam", "value"),
            Case("override", "createdInInstrumentationRunnerArguments"),
        )
        return cases.map { case ->
            dynamicTest(case.param) {
                assertThat(instrumentationArgs).containsEntry(case.param, case.expectedValue)
            }
        } + dynamicTest("keys count") {
            assertThat(instrumentationArgs.keys).hasSize(cases.size)
        }
    }

    private data class Case(
        val param: String,
        val expectedValue: String
    )
}
