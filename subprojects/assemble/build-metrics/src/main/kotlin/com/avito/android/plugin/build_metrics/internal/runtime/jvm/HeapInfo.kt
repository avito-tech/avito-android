package com.avito.android.plugin.build_metrics.internal.runtime.jvm

internal data class HeapInfo(
    val heap: MemoryUsage,
    val metaspace: MemoryUsage,
)

internal data class MemoryUsage(
    val usedKb: Int,
    /**
     * The amount of memory guaranteed to be available for use by the Java VM.
     * Some GCs can return it to the OS.
     */
    val committedKb: Int,
) {

    operator fun plus(increment: MemoryUsage): MemoryUsage {
        return MemoryUsage(
            usedKb = this.usedKb + increment.usedKb,
            committedKb = this.committedKb + increment.committedKb,
        )
    }
}
