package com.avito.android.plugin.build_metrics.internal.runtime.jvm

import com.avito.android.plugin.build_metrics.internal.core.BuildMetric
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm.GradleDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm.GradleWorker
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm.KotlinDaemon
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.LocalVm.Unknown
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory

internal interface JvmMetricsSender {
    fun send(vm: LocalVm, heapInfo: HeapInfo)
}

internal class JvmMetricsSenderImpl(
    private val tracker: BuildMetricSender,
    factory: LoggerFactory,
) : JvmMetricsSender {

    private val logger: Logger = factory.create("JvmMetricsSender")

    override fun send(vm: LocalVm, heapInfo: HeapInfo) {
        metrics(vm, heapInfo).forEach { metric ->
            tracker.send(metric)
        }
    }

    private fun metrics(vm: LocalVm, heapInfo: HeapInfo): List<BuildMetric> {
        return listOf(
            // Use time metrics instead of gauges to collect statistics for a collection of similar processes.
            // Namely, Gradle workers and similar.
            BuildJvmHeapUsedMetric(processName(vm), heapInfo.heap.usedKb.toLong()),
            BuildJvmHeapCommittedMetric(processName(vm), heapInfo.heap.committedKb.toLong()),
            BuildJvmMetaspaceUsedMetric(processName(vm), heapInfo.metaspace.usedKb.toLong()),
            BuildJvmMetaspaceCommittedMetric(processName(vm), heapInfo.metaspace.committedKb.toLong()),
        )
    }

    private fun processName(vm: LocalVm): String {
        return when (vm) {
            is GradleDaemon -> "gradle_daemon"
            is GradleWorker -> "gradle_worker"
            is KotlinDaemon -> "kotlin_daemon"
            is Unknown -> {
                logger.warn("Unknown jvm process ${vm.name}. Add it to sealed class as known process type")
                "unknown"
            }
        }
    }
}
