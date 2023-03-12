package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.graphite.GraphiteMetric
import com.avito.android.plugin.build_metrics.internal.core.StubBuildMetricsSender
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.HeapInfo
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.JvmMetricsSenderImpl
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm.KotlinDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm.Unknown
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.MemoryUsage
import com.avito.graphite.series.SeriesName
import com.avito.logger.PrintlnLoggerFactory
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class JvmMetricsSenderTest {

    private val buildMetricSender = StubBuildMetricsSender()

    @Test
    fun `send - Gradle daemon`() {
        send(GradleDaemon(id = 1))

        val metric = buildMetricSender.getSentGraphiteMetrics().first()
        assertThat(metric.path.asAspect()).endsWith(";process_name=gradle_daemon")
    }

    @Test
    fun `send - Kotlin daemon`() {
        send(KotlinDaemon(id = 1))

        val metric = buildMetricSender.getSentGraphiteMetrics().first()
        assertThat(metric.path.asAspect()).endsWith(";process_name=kotlin_daemon")
    }

    @Test
    fun `send - Gradle worker`() {
        send(GradleWorker(id = 1))

        val metric = buildMetricSender.getSentGraphiteMetrics().first()
        assertThat(metric.path.asAspect()).endsWith(";process_name=gradle_worker")
    }

    @Test
    fun `send - unknown JVM process`() {
        send(Unknown(id = 1, name = "app.Worker"))

        val metric = buildMetricSender.getSentGraphiteMetrics().first()
        assertThat(metric.path.asAspect()).endsWith(";process_name=unknown")
    }

    @Test
    fun `send - format memory metrics`() {
        send(
            vm = GradleDaemon(id = 1),
            heapInfo = HeapInfo(
                heap = MemoryUsage(usedKb = 100, committedKb = 200),
                metaspace = MemoryUsage(usedKb = 50, committedKb = 60)
            )
        )

        val sentMetrics = buildMetricSender.getSentGraphiteMetrics()

        assertThat(sentMetrics).hasSize(4)
        assertThat(sentMetrics).contains(
            GraphiteMetric(
                SeriesName.create("jvm.memory.heap.used", multipart = true)
                    .addTag("process_name", "gradle_daemon"),
                "100"
            )
        )
        assertThat(sentMetrics).contains(
            GraphiteMetric(
                SeriesName.create("jvm.memory.heap.committed", multipart = true)
                    .addTag("process_name", "gradle_daemon"),
                "200"
            )
        )
        assertThat(sentMetrics).contains(
            GraphiteMetric(
                SeriesName.create("jvm.memory.metaspace.used", multipart = true)
                    .addTag("process_name", "gradle_daemon"),
                "50"
            )
        )
        assertThat(sentMetrics).contains(
            GraphiteMetric(
                SeriesName.create("jvm.memory.metaspace.committed", multipart = true)
                    .addTag("process_name", "gradle_daemon"),
                "60"
            )
        )
    }

    private fun send(
        vm: LocalVm,
        heapInfo: HeapInfo = stubHeapInfo()
    ) =
        JvmMetricsSenderImpl(buildMetricSender, PrintlnLoggerFactory).send(vm, heapInfo)

    private fun stubHeapInfo() = HeapInfo(
        heap = MemoryUsage(usedKb = 1, committedKb = 1),
        metaspace = MemoryUsage(usedKb = 1, committedKb = 1)
    )
}
