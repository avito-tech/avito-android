package com.avito.emcee

import com.avito.emcee.internal.EmceeConfigTestHelper
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.Path

internal class EmceePluginTest {

    @Test
    fun `configuration - passes - without emcee extension configured`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.emcee")
                    }
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "help").assertThat().buildSuccessful()
    }

    @TestFactory
    fun `configuration - all args passed correctly`(@TempDir projectDir: File): List<DynamicTest> {
        val appModuleName = "app"
        val outputDirName = "emcee"

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = appModuleName,
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.emcee")
                    },
                    useKts = true,
                    imports = listOf("import java.time.Duration"),
                    buildGradleExtra = """
                    |emcee {
                    |   job.id.set("AvitoComponentTests#PR-2214")
                    |   job.groupId.set("PRTests#PR-2214")
                    |   job.priority.set(100)
                    |   job.groupPriority.set(100)
                    |   retries.set(2)
                    |   deviceApis.add(22)
                    |   deviceApis.add(30)
                    |   testTimeout.set(Duration.ofSeconds(120))
                    |   queueBaseUrl.set("http://emcee.queue")
                    |   configTestMode.set(true)
                    |   outputDir.set(project.layout.buildDirectory.dir("$outputDirName"))
                    |}
                    |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "emceeTestDebug").assertThat().buildSuccessful()

        val outputDir = Path(projectDir.path, appModuleName, "build", outputDirName).toFile()
        val config: EmceeTestActionConfig = EmceeConfigTestHelper(outputDir).deserialize()

        return listOf(
            Case("job id") { assertThat(job.id).isEqualTo("AvitoComponentTests#PR-2214") },
            Case("job group id") { assertThat(job.groupId).isEqualTo("PRTests#PR-2214") },
            Case("job priority") { assertThat(job.priority).isEqualTo(100) },
            Case("job group priority") { assertThat(job.groupPriority).isEqualTo(100) },
            Case("retries") { assertThat(testExecutionBehavior.retries).isEqualTo(2) },
            Case("devices") { assertThat(devices.map { it.runtime }).containsExactly("22", "30") },
            Case("single test timeout") { assertThat(timeoutConfiguration.singleTestMaximumDuration).isEqualTo(120) },
        ).map { case -> DynamicTest.dynamicTest(case.paramName) { case.assertion(config) } }
    }

    data class Case(val paramName: String, val assertion: EmceeTestActionConfig.() -> Unit)
}
