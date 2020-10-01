package com.avito.android.build_verdict

internal data class GsonableError(
    val message: String,
    val stackTrace: String
)

internal data class BuildVerdict(
    val rootError: GsonableError,
    val failedTasks: List<FailedTask>
)

internal data class FailedTask(
    val name: String,
    val projectPath: String,
    val errorOutput: String,
    val originalError: GsonableError
)
