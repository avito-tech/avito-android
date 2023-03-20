@file:Suppress("MaxLineLength")
package com.avito.android.plugin.build_metrics

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val rootAppName = "root"

internal class BuildMetricsPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
        TestProjectGenerator(
            name = rootAppName,
            plugins = plugins {
                id("com.avito.android.build-metrics")
                id("com.avito.android.gradle-logger")
            },
            imports = listOf(
                "import com.avito.android.plugin.build_metrics.BuildEnvironment",
            ),
            buildGradleExtra = """
                |buildMetrics {
                |   buildType.set("test")
                |   environment.set(BuildEnvironment.CI)
                |}
            """.trimMargin(),
            modules = listOf(
                AndroidAppModule(name = "app")
            ),
        ).generateIn(tempDir)
    }

    @Test
    fun `send configuration time - build`() {
        val result = build(":app:preBuild")

        result.assertThat()
            .buildSuccessful()

        result.assertHasMetric("build.metrics.test.builds.gradle.build.init_configuration;build_type=test;env=ci")
    }

    @Test
    fun `send total build time - build`() {
        val result = build(":app:preBuild")

        result.assertThat()
            .buildSuccessful()

        result.assertHasMetric("build.metrics.test.builds.gradle.build;build_type=test;env=ci")
    }

    @TestFactory
    fun `send app build time - build app`(): List<DynamicTest> {
        class Metric(
            val name: String,
            val count: Int,
        )
        class Case(
            val description: String,
            val tasks: Array<String>,
            val metrics: List<Metric>
        )
        return listOf(
            Case(
                description = "build app",
                tasks = arrayOf(":app:assembleDebug"),
                metrics = listOf(
                    Metric(
                        name = "build.metrics.test.builds.gradle.task.type.PackageApplication;build_type=test;env=ci;module_name=app;app_type=main;build_status=success",
                        count = 1,
                    )
                ),
            ),
            Case(
                description = "run instrumentation tests",
                tasks = arrayOf(":app:assembleDebug", ":app:assembleDebugAndroidTest"),
                metrics = listOf(
                    Metric(
                        name = "build.metrics.test.builds.gradle.task.type.PackageApplication;build_type=test;env=ci;module_name=app;app_type=main;build_status=success",
                        count = 1,
                    ),
                    Metric(
                        name = "build.metrics.test.builds.gradle.task.type.PackageApplication;build_type=test;env=ci;module_name=app;app_type=test;build_status=success",
                        count = 1,
                    )
                ),
            ),
        ).map {
            dynamicTest("send total task time for app scenario - " + it.description) {
                val result = build(*it.tasks)

                result.assertThat()
                    .buildSuccessful()

                it.metrics.forEach { metric ->
                    result.assertHasMetric(metric.name) { hasSize(metric.count) }
                }
            }
        }
    }

    private fun build(vararg args: String) =
        BuildMetricsRunner(projectDir)
            .build(args.toList())
}
