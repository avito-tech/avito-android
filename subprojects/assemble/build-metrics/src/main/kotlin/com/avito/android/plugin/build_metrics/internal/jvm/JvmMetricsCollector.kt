package com.avito.android.plugin.build_metrics.internal.jvm

import com.avito.android.Result
import com.avito.android.Result.Failure
import com.avito.android.plugin.build_metrics.internal.jvm.command.Jcmd
import org.slf4j.LoggerFactory
import java.util.stream.Collectors.toMap

/**
 * Collects JVM metrics by CLI JDK tools.
 *
 * Alternative implementations:
 *
 * - Instrument processes by java agents.
 *   Blocker: we have no full control on spawning VMs inside Gradle and plugins.
 * - Connect to VMs by JMX and read memory beans.
 *   Blocker: failed to make connections stable, so
 *   Inconvenience: uses internal classes from jdk.management.agent module (--add-opens in Gradle jvm args)
 */
internal class JvmMetricsCollector(
    private val vmResolver: VmResolver,
    private val jcmd: Jcmd
) {

    private val log = LoggerFactory.getLogger(JvmMetricsCollector::class.java)

    fun collect(): Result<Map<LocalVm, HeapInfo>> {
        return vmResolver.localVMs()
            .map { vms ->
                @Suppress("UNCHECKED_CAST")
                vms.parallelStream()
                    .map { it to getHeapInfo(it) }
                    .filter { it.second != null }
                    .collect(toMap(Pair<*, *>::first, Pair<*, *>::second)) as Map<LocalVm, HeapInfo>
            }
    }

    /**
     * @return null in case of error.
     *
     * There are some expected errors:
     * - process may be killed already
     * - process may have unsupported GC
     *
     * We'd like to preserve as much as possible metrics from other processes
     */
    private fun getHeapInfo(vm: LocalVm): HeapInfo? {
        val result = jcmd.gcHeapInfo(vm.id)
        return if (result.isFailure()) {
            log.warn("Failed to get heap info for $vm", (result as Failure).throwable)
            null
        } else {
            result.getOrThrow()
        }
    }
}
