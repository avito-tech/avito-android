package com.avito.android.plugin.build_metrics.internal

import org.gradle.api.Task
import org.gradle.util.Path

internal class TaskExecutionResult(
    val name: String,
    val path: Path,
    val type: Class<out Task>,
    val startMs: Long,
    val endMs: Long,
    val cacheResult: TaskCacheResult,
    val tags: Map<String, String> = emptyMap(),
) {
    val elapsedMs: Long
        get() = endMs - startMs
}

internal sealed class TaskCacheResult {

    data object Disabled : TaskCacheResult()

    class Miss(
        val local: Boolean,
        val remote: Boolean
    ) : TaskCacheResult()

    sealed class Hit : TaskCacheResult() {
        data object Local : Hit()
        data object Remote : Hit()
    }
}
