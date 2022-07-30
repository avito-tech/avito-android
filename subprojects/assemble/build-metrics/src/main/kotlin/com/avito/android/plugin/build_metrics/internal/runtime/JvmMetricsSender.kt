package com.avito.android.plugin.build_metrics.internal.runtime

import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.KotlinDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.LocalVm.Unknown
import com.avito.android.stats.SeriesName
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.StatsMetric

internal interface JvmMetricsSender {
    fun send(vm: LocalVm, heapInfo: HeapInfo)
}

internal class JvmMetricsSenderImpl(
    private val tracker: StatsDSender
) : JvmMetricsSender {

    override fun send(vm: LocalVm, heapInfo: HeapInfo) {
        metrics(vm, heapInfo).forEach { metric ->
            tracker.send(metric)
        }
    }

    private fun metrics(vm: LocalVm, heapInfo: HeapInfo): List<StatsMetric> {
        val name = SeriesName
            .create("jvm", "memory")
            .append(processName(vm))

        return listOf(
            // Use time metrics instead of gauges to collect statistics for a collection of similar processes.
            // Namely, Gradle workers and similar.
            StatsMetric.time(name.append("heap", "used"), heapInfo.heap.usedKb.toLong()),
            StatsMetric.time(name.append("heap", "committed"), heapInfo.heap.committedKb.toLong()),

            StatsMetric.time(name.append("metaspace", "used"), heapInfo.metaspace.usedKb.toLong()),
            StatsMetric.time(name.append("metaspace", "committed"), heapInfo.metaspace.committedKb.toLong()),
        )
    }

    private fun processName(vm: LocalVm): SeriesName {
        val name = when (vm) {
            is GradleDaemon -> "gradle_daemon"
            is GradleWorker -> "gradle_worker"
            is KotlinDaemon -> "kotlin_daemon"
            is Unknown -> vm.name
                .takeLast(64)
                .removePrefix(".")
                .lowercase()
        }
        return SeriesName.create(name, multipart = false)
    }
}
