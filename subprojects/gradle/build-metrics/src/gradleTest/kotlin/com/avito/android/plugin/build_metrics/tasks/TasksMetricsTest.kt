package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.plugin.build_metrics.BuildMetricsRunner
import com.avito.android.plugin.build_metrics.assertHasMetric
import com.avito.android.stats.TimeMetric
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
            },
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

        result.assertHasMetric<TimeMetric>(".build.tasks.cumulative.any")

        result.assertHasMetric<TimeMetric>(".build.tasks.slow.type.KotlinCompile")
        result.assertHasMetric<TimeMetric>(".build.tasks.slow.module.lib")
        result.assertHasMetric<TimeMetric>(".build.tasks.slow.task.lib.KotlinCompile")

        result.assertHasMetric<TimeMetric>(".build.tasks.critical.task.lib.KotlinCompile")
    }

    private fun build(vararg args: String) =
        BuildMetricsRunner(projectDir)
            .build(args.toList())
}
