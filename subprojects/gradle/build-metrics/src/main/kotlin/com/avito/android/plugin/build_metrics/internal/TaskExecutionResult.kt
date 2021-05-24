package com.avito.android.plugin.build_metrics.internal

import org.gradle.api.Task
import org.gradle.util.Path

internal class TaskExecutionResult(
    val path: Path,
    val type: Class<out Task>,
    val cacheResult: TaskCacheResult,
)

internal sealed class TaskCacheResult {

    object Disabled : TaskCacheResult()

    class Miss(
        val local: Boolean,
        val remote: Boolean
    ) : TaskCacheResult()

    sealed class Hit : TaskCacheResult() {
        object Local : Hit()
        object Remote : Hit()
    }
}
