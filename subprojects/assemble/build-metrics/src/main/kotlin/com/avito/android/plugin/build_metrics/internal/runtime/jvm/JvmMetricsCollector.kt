package com.avito.android.plugin.build_metrics.internal.runtime.jvm

import com.avito.android.Result
import com.avito.android.isFailure
import com.avito.android.plugin.build_metrics.internal.runtime.MetricsCollector
import com.avito.android.plugin.build_metrics.internal.runtime.jvm.command.Jcmd
import org.slf4j.LoggerFactory

/**
 * Collects JVM metrics by CLI JDK tools.
 *
 * Alternative implementations:
 *
 * - Instrument processes by java agents.
 *   Blocker: we have no full control on spawning VMs inside Gradle and plugins.
 * - Connect to VMs by JMX and read memory beans.
 *   Blocker: failed to make connections stable
 *   Inconvenience: uses internal classes from jdk.management.agent module (--add-opens in Gradle jvm args)
 */
internal class JvmMetricsCollector(
    private val vmResolver: VmResolver,
    private val jcmd: Jcmd,
    private val sender: JvmMetricsSender
) : MetricsCollector {

    private val log = LoggerFactory.getLogger(JvmMetricsCollector::class.java)

    override fun collect(): Result<Unit> {
        return vmResolver.localVMs()
            .map { vms ->
                vms.parallelStream()
                    .map { it to getHeapInfo(it) }
                    .forEach { (vm, heapInfo) ->
                        if (heapInfo != null) {
                            sender.send(vm, heapInfo)
                        }
                    }
            }
            .map { }
    }

    /**
     * @return null in case of error.
     *
     * There are some expected errors:
     * - process is killed already
     * - process has unsupported GC
     *
     * We'd like to preserve as much as possible metrics from other processes
     */
    private fun getHeapInfo(vm: LocalVm): HeapInfo? {
        val result = jcmd.gcHeapInfo(vm.id)
        return if (result.isFailure()) {
            log.warn("Failed to get heap info for $vm", result.throwable)
            null
        } else {
            result.getOrThrow()
        }
    }
}
