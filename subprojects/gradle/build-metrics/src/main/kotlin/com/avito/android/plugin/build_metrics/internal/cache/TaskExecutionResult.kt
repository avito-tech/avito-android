package com.avito.android.plugin.build_metrics.internal.cache

import org.gradle.api.Task

internal class TaskExecutionResult(
    val path: String,
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
