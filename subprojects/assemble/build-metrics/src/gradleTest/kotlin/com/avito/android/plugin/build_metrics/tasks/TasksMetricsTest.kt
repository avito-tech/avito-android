@file:Suppress("MaxLineLength")
package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.plugin.build_metrics.BuildMetricsRunner
import com.avito.android.plugin.build_metrics.assertHasMetric
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TasksMetricsTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.build-metrics")
                id("com.avito.android.gradle-logger")
            },
            imports = listOf(
                "import com.avito.android.plugin.build_metrics.BuildEnvironment",
                "import java.time.Duration",
            ),
            buildGradleExtra = """
                |buildMetrics {
                |   buildType.set("test")
                |   environment.set(BuildEnvironment.CI)
                |   slowTaskMinimumDuration.set(Duration.ofMillis(0))
                |}
            """.trimMargin(),
            modules = listOf(
                KotlinModule(name = "lib")
            )
        ).generateIn(tempDir)
    }

    @Test
    fun `build - send tasks metrics`() {
        val result = build(":lib:compileKotlin")

        result.assertThat()
            .buildSuccessful()

        result.assertHasMetric("build.metrics.test.builds.gradle.tasks;build_type=test;env=ci")

        result.assertHasMetric("build.metrics.test.builds.gradle.slow.task.type;build_type=test;env=ci;task_type=KotlinCompile")
        result.assertHasMetric("build.metrics.test.builds.gradle.slow.module;build_type=test;env=ci;module_name=lib")
        result.assertHasMetric("build.metrics.test.builds.gradle.slow.module.task.type;build_type=test;env=ci;module_name=lib;task_type=KotlinCompile")

        result.assertHasMetric("build.metrics.test.builds.gradle.critical.module.task.type;build_type=test;env=ci;module_name=lib;task_type=KotlinCompile")
        result.assertHasMetric("build.metrics.test.builds.gradle.critical.task.type;build_type=test;env=ci;task_type=KotlinCompile")
    }

    private fun build(vararg args: String) =
        BuildMetricsRunner(projectDir)
            .build(args.toList())
}
