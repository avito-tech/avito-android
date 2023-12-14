package com.avito.instrumentation

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class InstrumentationTestsPluginCIFalseTests {

    @field:TempDir
    lateinit var projectDir: File

    private val instrumentationTask = ":app:instrumentationApi22Test"

    @Test
    fun `k8s emulator target task is available with credentials`() {
        createProject(
            projectDir = projectDir,
            device = """
                |Device.CloudEmulator(
                |    name = "api22",
                |    api = 22,
                |    model = "Android_SDK_built_for_x86",
                |    image = "stub",
                |    gpu = false,
                |    cpuCoresLimit = "1",
                |    cpuCoresRequest = "1.3",
                |    memoryLimit = "3.5Gi",
                |    memoryRequest = "3Gi",
                |)
                |""".trimMargin()
        )

        executeInstrumentationTask(
            instrumentationTask,
            false,
            "-PkubernetesToken=stub",
            "-PkubernetesUrl=stub",
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(instrumentationTask)
    }

    @Test
    fun `gradle configuration fails with k8s emulator target without credentials`() {
        createProject(
            projectDir = projectDir,
            device = """
                |Device.CloudEmulator(
                |    name = "api22",
                |    api = 22,
                |    model = "Android_SDK_built_for_x86",
                |    image = "stub",
                |    gpu = false,
                |    cpuCoresLimit = "1",
                |    cpuCoresRequest = "1.3",
                |    memoryLimit = "3.5Gi",
                |    memoryRequest = "3Gi",
                |)
                |""".trimMargin()
        )
        executeInstrumentationTask(
            task = instrumentationTask,
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("Cannot query the value of property")
    }

    @Test
    fun `local emulator target task is always available`() {
        createProject(
            projectDir = projectDir,
            device = "Device.LocalEmulator.device(22)"
        )

        executeInstrumentationTask(
            task = ":app:instrumentationApi22Local",
            expectFailure = false
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":app:instrumentationApi22Local")
    }

    private fun executeInstrumentationTask(
        task: String,
        expectFailure: Boolean,
        vararg params: String
    ): TestResult = gradlew(
        projectDir,
        task,
        *params,
        "-Pavito.build=local",
        "-Pavito.git.state=local",
        "-Pci=false",
        expectFailure = expectFailure,
        dryRun = true
    )

    private fun createProject(
        projectDir: File,
        device: String
    ) {
        TestProjectGenerator(
            plugins = plugins {
                  id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    buildGradleExtra = instrumentationConfiguration(
                        device = device
                    ),
                    useKts = true,
                )
            )
        ).generateIn(projectDir)
    }

    private fun instrumentationConfiguration(device: String): String = """
    |import com.avito.instrumentation.reservation.request.Device
    |import com.avito.instrumentation.configuration.KubernetesViaCredentials
    |import com.avito.kotlin.dsl.getOptionalStringProperty
    |import com.avito.instrumentation.configuration.report.ReportConfig
    |
    |instrumentation {
    |
    |    outputDir.set(rootProject.file("outputs"))
    |    report.set(ReportConfig.NoOp)
    |    environments {
    |       register<KubernetesViaCredentials>("test") {
    |           url.set("http://stub")
    |           namespace.set("android-emulator")
    |           token.set(getOptionalStringProperty("kubernetesToken"))
    |       }
    |    }
    |
    |    configurations {
    |        register("api22") {
    |            targets {
    |                register("api22") {
    |                    deviceName = "api22"
    |
    |                    scheduling {
    |                        quota {
    |                            minimumSuccessCount = 1
    |                        }
    |
    |                        staticDevicesReservation {
    |                            device = $device
    |                            count = 1
    |                        }
    |                    }
    |                }
    |            }
    |        }
    |    }
    |}
    |android {
    |    defaultConfig {
    |        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    |    }
    |}
    |""".trimMargin()
}
