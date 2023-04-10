package com.avito.android.plugin.build_metrics.tasks

import com.avito.android.plugin.build_metrics.assertHasMetric
import com.avito.test.gradle.module.AndroidAppModule
import org.junit.jupiter.api.Test

class CompileTasksMetricsTest : BaseTasksMetricsTest(
    sendSlow = false,
    sendCritical = false,
    sendCompile = true,
    mainModule = AndroidAppModule(
        name = "app",
        enableKapt = true,
        enableKsp = true,
    )
) {

    @Test
    @Suppress("MaxLineLength")
    fun `execute compile - metrics exist`() {
        val result = build(":app:assembleDebug")

        result.assertThat()
            .buildSuccessful()

        result.assertHasMetric("build.metrics.test.builds.gradle.task.type.KotlinCompile;build_type=test;env=ci;module_name=app;task_name=compileDebugKotlin")
        result.assertHasMetric("build.metrics.test.builds.gradle.task.type.JavaCompile;build_type=test;env=ci;module_name=app;task_name=compileDebugJavaWithJavac")
        result.assertHasMetric("build.metrics.test.builds.gradle.task.type.KaptGenerateStubs;build_type=test;env=ci;module_name=app;task_name=kaptGenerateStubsDebugKotlin")
        result.assertHasMetric("build.metrics.test.builds.gradle.task.type.KaptWithoutKotlinc;build_type=test;env=ci;module_name=app;task_name=kaptDebugKotlin")
        result.assertHasMetric("build.metrics.test.builds.gradle.task.type.KspJvm;build_type=test;env=ci;module_name=app;task_name=kspDebugKotlin")
    }
}
