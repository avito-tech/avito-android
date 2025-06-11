package com.avito.android.plugin.build_metrics.internal

import com.avito.android.plugin.build_metrics.internal.result.BuildResult

internal class BuildOperationsResult(
    val tasksExecutions: List<TaskExecutionResult>,
    val cacheOperations: CacheOperations,
    val buildResult: BuildResult,
)

internal class CacheOperations(
    val errors: List<RemoteBuildCacheError>,
)

internal class RemoteBuildCacheError(
    val selector: Selector,
    val cause: Throwable
) {
    data class Selector(
        val type: BuildCacheOperationType,
        val httpStatus: Int?,
    ) {
        val code: String
            get() = type.code

        val errorType: String
            get() = httpStatus?.toString() ?: "unknown"
    }
}

internal enum class BuildCacheOperationType(
    val code: String
) {
    LOAD("load"), STORE("store")
}
