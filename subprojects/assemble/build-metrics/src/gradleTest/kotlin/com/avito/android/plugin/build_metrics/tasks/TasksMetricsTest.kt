@file:Suppress("MaxLineLength")

package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.plugin.build_metrics.assertHasMetric
import com.avito.test.gradle.module.KotlinModule
import org.junit.jupiter.api.Test

internal class TasksMetricsTest : BaseTasksMetricsTest(
    sendSlow = true,
    sendCritical = true,
    sendCompile = false,
    mainModule = KotlinModule(name = "lib")
) {

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
}
