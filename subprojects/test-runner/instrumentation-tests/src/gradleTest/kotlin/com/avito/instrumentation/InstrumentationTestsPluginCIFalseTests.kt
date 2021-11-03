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

    private val instrumentationTask = ":app:instrumentationApi22Default"

    @Test
    fun `k8s emulator target task is available with credentials`() {
        createProject(
            projectDir = projectDir,
            device = """
                |new CloudEmulator(
                |    "api22",
                |    22,
                |    "Android_SDK_built_for_x86",
                |    "stub",
                |    false,
                |    "1",
                |    "1.3",
                |    "3.5Gi",
                |    "3Gi"
                |)
                |""".trimMargin()
        )

        executeInstrumentationTask(
            instrumentationTask,
            false,
            "-PkubernetesToken=stub",
            "-PkubernetesUrl=stub",
            "-PkubernetesCaCertData=stub"
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(instrumentationTask)
    }

    @Test
    fun `gradle configuration fails with k8s emulator target without credentials`() {
        createProject(
            projectDir = projectDir,
            device = """
                |new CloudEmulator(
                |    "api22",
                |    22,
                |    "Android_SDK_built_for_x86",
                |    "stub",
                |    false,
                |    "1",
                |    "1.3",
                |    "3.5Gi",
                |    "3Gi"
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
            device = "LocalEmulator.device(22)"
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
                    )
                )

            )
        ).generateIn(projectDir)
    }

    private fun instrumentationConfiguration(device: String): String = """
    |import static com.avito.instrumentation.reservation.request.Device.LocalEmulator
    |import com.avito.instrumentation.reservation.request.Device.CloudEmulator
    |
    |instrumentation {
    |
    |    output = rootProject.file("outputs").path
    |
    |    configurations {
    |
    |        api22 {
    |            targets {
    |                api22 {
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
    |        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    |    }
    |}
    |""".trimMargin()
}
