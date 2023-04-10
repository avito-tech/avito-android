package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.plugin.build_metrics.BuildMetricsRunner
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.Module
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class BaseTasksMetricsTest(
    private val sendSlow: Boolean,
    private val sendCritical: Boolean,
    private val sendCompile: Boolean,
    private val mainModule: Module,
) {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        println(tempDir)
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
                |   sendCriticalPathMetrics.set($sendCritical)
                |   sendSlowTaskMetrics.set($sendSlow)
                |   sendCompileMetrics.set($sendCompile)
                |   slowTaskMinimumDuration.set(Duration.ofMillis(0))
                |   criticalTaskMinimumDuration.set(Duration.ofMillis(0))
                |   compileMetricsMinimumDuration.set(Duration.ofMillis(0))
                |}
            """.trimMargin(),
            modules = listOf(mainModule)
        ).generateIn(tempDir)
    }

    protected fun build(vararg args: String) =
        BuildMetricsRunner(projectDir)
            .build(args.toList())
}
