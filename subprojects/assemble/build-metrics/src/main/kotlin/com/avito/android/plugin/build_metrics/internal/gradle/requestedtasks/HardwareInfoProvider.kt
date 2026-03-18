package com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks

import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

internal open class HardwareInfoProvider {

    open val hardwareInfo: HardwareInfo by lazy { collect() }

    private fun collect(): HardwareInfo {
        val ramBytes = totalMemoryBytes()
        val ramGb = roundToStandardRamGb(ramBytes)

        return HardwareInfo(ramGb = ramGb)
    }

    private fun totalMemoryBytes(): Long {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        return osBean.totalMemorySize
    }

    companion object {
        private val STANDARD_RAM_SIZES = listOf(16, 24, 32, 48, 64, 96, 128, 256)

        internal fun roundToStandardRamGb(bytes: Long): Int {
            val gb = (bytes.toDouble() / (1024L * 1024 * 1024)).toInt()
            return STANDARD_RAM_SIZES
                .sortedBy { kotlin.math.abs(it - gb) }
                .take(2)
                .let { candidates ->
                    if (candidates.size == 2 &&
                        kotlin.math.abs(candidates[0] - gb) == kotlin.math.abs(candidates[1] - gb)
                    ) {
                        candidates.max()
                    } else {
                        candidates.first()
                    }
                }
        }
    }
}

internal data class HardwareInfo(
    val ramGb: Int,
)
