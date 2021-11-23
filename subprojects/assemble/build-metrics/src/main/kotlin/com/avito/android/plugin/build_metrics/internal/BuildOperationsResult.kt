package com.avito.android.plugin.build_metrics.internal

internal class BuildOperationsResult(
    val tasksExecutions: List<TaskExecutionResult>,
    val cacheOperations: CacheOperations
)

internal class CacheOperations(
    val errors: List<RemoteBuildCacheError>,
)

internal class RemoteBuildCacheError(
    val type: BuildCacheOperationType,
    val httpStatus: Int?,
    val cause: Throwable
)

internal enum class BuildCacheOperationType {
    LOAD, STORE
}
