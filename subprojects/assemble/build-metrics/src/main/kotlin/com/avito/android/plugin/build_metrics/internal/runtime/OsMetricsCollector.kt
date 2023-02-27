package com.avito.android.plugin.build_metrics.internal.runtime

import com.avito.android.Result
import com.sun.management.OperatingSystemMXBean
import org.gradle.api.JavaVersion
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory

internal class OsMetricsCollector(
    private val cgroup: Cgroup2,
    private val sender: OsMetricsSender
) : MetricsCollector {

    private val log = LoggerFactory.getLogger(OsMetricsCollector::class.java)

    override fun collect(): Result<Unit> {
        return memoryInfo()
            .map {
                sender.send(it)
            }
            .map { }
    }

    private fun memoryInfo(): Result<MemoryInfo> {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

        if (osBean.isContainerAware()) {
            return Result.Success(osBean.toMemoryInfo())
        }
        return when (cgroup) {
            is Cgroup2.Available ->
                Result.tryCatch {
                    MemoryInfo(
                        usedKb = cgroup.memory.memoryCurrentBytes.bytesToKiB(),
                        totalKb = cgroup.memory.memoryMaxBytes.bytesToKiB(),
                    )
                }.rescue { error ->
                    log.warn("Error while reading cgroups. Fallback to not container aware metrics", error)
                    Result.Success(osBean.toMemoryInfo())
                }
            is Cgroup2.Unavailable ->
                Result.Success(osBean.toMemoryInfo())
        }
    }

    @Suppress("DEPRECATION")
    private fun OperatingSystemMXBean.toMemoryInfo() =
        MemoryInfo(
            usedKb = (totalMemorySize - freeMemorySize).bytesToKiB(),
            totalKb = totalMemorySize.bytesToKiB()
        )

    /**
     * https://bugs.openjdk.org/browse/JDK-8228428
     */
    @Suppress("unused")
    private fun OperatingSystemMXBean.isContainerAware() =
        JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)
}
