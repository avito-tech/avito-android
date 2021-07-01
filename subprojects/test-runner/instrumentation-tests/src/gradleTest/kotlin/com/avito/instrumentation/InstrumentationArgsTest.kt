package com.avito.instrumentation

import com.avito.instrumentation.internal.AndroidInstrumentationArgsDumper
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
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

@OptIn(ExperimentalPathApi::class)
internal class InstrumentationArgsTest {

    private val appName = "app"

    @TestFactory
    fun `instrumentation args - passed correctly - kotlin`(@TempDir projectDir: File): List<DynamicTest> {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = appName,
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    useKts = true,
                    imports = listOf(
                        "import com.avito.instrumentation.reservation.request.Device"
                    ),
                    buildGradleExtra = kotlinStubConfig
                )
            ),
            useKts = true,
        ).generateIn(projectDir)

        return cases(projectDir)
    }

    @TestFactory
    fun `instrumentation args - passed correctly - groovy`(@TempDir projectDir: File): List<DynamicTest> {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = appName,
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    imports = listOf(
                        "import static com.avito.instrumentation.reservation.request.Device.LocalEmulator"
                    ),
                    buildGradleExtra = groovyStubConfig
                )
            )
        ).generateIn(projectDir)

        return cases(projectDir)
    }

    private fun cases(projectDir: File): List<DynamicTest> {
        val commit = projectDir.git("rev-parse HEAD").trim()

        ciRun(
            projectDir,
            "help",
            "-PteamcityBuildId=0",
            "-Pavito.git.state=env",
            "-PisGradleTestKitRun=true",
        ).assertThat().buildSuccessful()

        val dumpDir = projectDir.toPath() / appName / "build" / "test-runner" / dumpDirName

        val instrumentationArgs = AndroidInstrumentationArgsDumper(dumpDir.toFile()).readDump()

        return listOf(
            Case("planSlug", "AppAndroid"),
            // local jobSlug differs from testRunner, can't parse params on configuration level
            Case("jobSlug", "LocalTests"),
            Case("runId", "stub.$commit.teamcity-BT"),
            Case("teamcityBuildId", "0"),
            Case("avito.report.enabled", "false"),
            Case("fileStorageUrl", "http://stub"),
            Case("sentryDsn", "stub"),
            Case("deviceName", "local"),
            Case("reportApiUrl", "http://stub"),
            Case("reportViewerUrl", "http://stub"),
        ).map { case ->
            dynamicTest(case.param) {
                assertThat(instrumentationArgs).containsEntry(case.param, case.expectedValue)
            }
        }
    }

    private data class Case(
        val param: String,
        val expectedValue: String
    )
}
