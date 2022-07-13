package com.avito.android.plugin.build_metrics.jvm

import com.avito.android.plugin.build_metrics.BuildMetricsRunner
import com.avito.android.plugin.build_metrics.assertHasMetric
import com.avito.android.stats.TimeMetric
import com.avito.test.Flaky
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.IterableSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@Flaky("MBSA-648")
internal class JvmMetricsTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.build-metrics")
            },
            buildGradleExtra = """
                tasks.register<Task>("delay") {
                    doLast {
                        Thread.sleep(3000)
                    }
                }
            """.trimIndent(),
            useKts = true
        ).generateIn(tempDir)
    }

    @Test
    fun `build - send jvm metrics`() {
        val result = build(":delay")

        result.assertThat()
            .buildSuccessful()

        val atLeastOne: IterableSubject.() -> Unit = { isNotEmpty() }

        result.assertHasMetric<TimeMetric>(".jvm.memory.gradle_daemon.heap.used", atLeastOne)
        result.assertHasMetric<TimeMetric>(".jvm.memory.gradle_daemon.heap.committed", atLeastOne)

        result.assertHasMetric<TimeMetric>(".jvm.memory.gradle_daemon.metaspace.used", atLeastOne)
        result.assertHasMetric<TimeMetric>(".jvm.memory.gradle_daemon.metaspace.committed", atLeastOne)

        result.assertHasMetric<TimeMetric>(".jvm.memory.gradle_worker.heap.used", atLeastOne)
        result.assertHasMetric<TimeMetric>(".jvm.memory.gradle_worker.heap.committed", atLeastOne)

        result.assertHasMetric<TimeMetric>(".jvm.memory.gradle_worker.metaspace.used", atLeastOne)
        result.assertHasMetric<TimeMetric>(".jvm.memory.gradle_worker.metaspace.committed", atLeastOne)
    }

    private fun build(vararg args: String) =
        BuildMetricsRunner(projectDir)
            .build(args.toList())
}
