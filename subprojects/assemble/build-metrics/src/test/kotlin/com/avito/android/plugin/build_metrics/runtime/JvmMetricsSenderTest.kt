package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.HeapInfo
import com.avito.android.plugin.build_metrics.internal.runtime.JvmMetricsSenderImpl
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.KotlinDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.Unknown
import com.avito.android.plugin.build_metrics.internal.runtime.MemoryUsage
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

        val sentMetrics = tracker.getSentMetrics()

        assertThat(sentMetrics).contains(
            TimeMetric(SeriesName.create("jvm.memory.gradle_daemon.heap.used", multipart = true), 100)
        )
        assertThat(sentMetrics).contains(
            TimeMetric(SeriesName.create("jvm.memory.gradle_daemon.heap.committed", multipart = true), 200)
        )
        assertThat(sentMetrics).contains(
            TimeMetric(SeriesName.create("jvm.memory.gradle_daemon.metaspace.used", multipart = true), 50)
        )
        assertThat(sentMetrics).contains(
            TimeMetric(SeriesName.create("jvm.memory.gradle_daemon.metaspace.committed", multipart = true), 60)
        )
        assertThat(sentMetrics).hasSize(4)
    }

    private fun send(
        vm: LocalVm,
        heapInfo: HeapInfo = stubHeapInfo()
    ) =
        JvmMetricsSenderImpl(tracker).send(vm, heapInfo)

    private fun stubHeapInfo() = HeapInfo(
        heap = MemoryUsage(usedKb = 1, committedKb = 1),
        metaspace = MemoryUsage(usedKb = 1, committedKb = 1)
    )
}
