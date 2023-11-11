package com.avito.instrumentation.runner.input_params

import com.avito.instrumentation.instrumentationPluginId
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.git
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RunnerInputParamsTest {

    @TestFactory
    fun `runner parameters with SendFromRunnerCase`(@TempDir projectDir: File): List<DynamicTest> {
        return test(SendFromRunnerCase(projectDir))
    }

    @TestFactory
    fun `runner parameters with NoOpReportCase`(@TempDir projectDir: File): List<DynamicTest> {
        return test(NoOpReportCase(projectDir))
    }

    @TestFactory
    fun `runner parameters with SendFromDeviceCase`(@TempDir projectDir: File): List<DynamicTest> {
        return test(SendFromDeviceCase(projectDir))
    }

    private fun test(
        case: Case,
    ): List<DynamicTest> {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = case.appModuleName,
                    packageName = case.appPackageName,
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    useKts = true,
                    imports = listOf(
                        "import com.avito.instrumentation.configuration.report.ReportConfig",
                        "import com.avito.instrumentation.reservation.request.Device",
                    ),
                    buildGradleExtra = case.buildScript
                )
            ),
            useKts = true,
        ).generateIn(case.projectDir)
        with(case.projectDir) {
            git("branch $case.targetBranch")
        }
        val buildResult = ciRun(
            case.projectDir,
            "-s",
            "app:instrumentationFunctionalK8sCredentials",
            "-PteamcityBuildId=0",
            "-Pavito.git.state=env",
            "-PisGradleTestKitRun=true",
            buildType = case.buildType,
            targetBranch = case.targetBranch
        )

        buildResult.assertThat().buildSuccessful()

        return case.assertions(
            commit = case.projectDir.git("rev-parse HEAD").trim()
        )
    }
}
