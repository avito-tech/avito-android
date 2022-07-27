package com.avito.android.plugin.build_metrics.jvm

import com.avito.android.plugin.build_metrics.internal.jvm.HeapInfo
import com.avito.android.plugin.build_metrics.internal.jvm.JvmMetricsSender
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm.KotlinDaemon
import com.avito.android.plugin.build_metrics.internal.jvm.LocalVm.Unknown
import com.avito.android.plugin.build_metrics.internal.jvm.MemoryUsage
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StubStatsdSender
import com.avito.android.stats.TimeMetric
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class JvmMetricsSenderTest {

    private val tracker = StubStatsdSender()

    @Test
    fun `send - Gradle daemon`() {
        send(GradleDaemon(id = 1))

        val metric = tracker.getSentMetrics().first()
        assertThat(metric.name.asAspect()).contains(".gradle_daemon.")
    }

    @Test
    fun `send - Kotlin daemon`() {
        send(KotlinDaemon(id = 1))

        val metric = tracker.getSentMetrics().first()
        assertThat(metric.name.asAspect()).contains(".kotlin_daemon.")
    }

    @Test
    fun `send - Gradle worker`() {
        send(GradleWorker(id = 1))

        val metric = tracker.getSentMetrics().first()
        assertThat(metric.name.asAspect()).contains(".gradle_worker.")
    }

    @Test
    fun `send - unknown JVM process`() {
        send(Unknown(id = 1, name = "app.Worker"))

        val metric = tracker.getSentMetrics().first()
        assertThat(metric.name.asAspect()).contains(".app_worker.")
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

        assertThat(tracker.getSentMetrics()).contains(
            TimeMetric(SeriesName.create("jvm.memory.gradle_daemon.heap.used", multipart = true), 100)
        )
        assertThat(tracker.getSentMetrics()).contains(
            TimeMetric(SeriesName.create("jvm.memory.gradle_daemon.heap.committed", multipart = true), 200)
        )
        assertThat(tracker.getSentMetrics()).contains(
            TimeMetric(SeriesName.create("jvm.memory.gradle_daemon.metaspace.used", multipart = true), 50)
        )
        assertThat(tracker.getSentMetrics()).contains(
            TimeMetric(SeriesName.create("jvm.memory.gradle_daemon.metaspace.committed", multipart = true), 60)
        )
        assertThat(tracker.getSentMetrics()).hasSize(4)
    }

    private fun send(
        vm: LocalVm,
        heapInfo: HeapInfo = stubHeapInfo()
    ) =
        JvmMetricsSender(tracker).send(vm, heapInfo)

    private fun stubHeapInfo() = HeapInfo(
        heap = MemoryUsage(usedKb = 1, committedKb = 1),
        metaspace = MemoryUsage(usedKb = 1, committedKb = 1)
    )
}
