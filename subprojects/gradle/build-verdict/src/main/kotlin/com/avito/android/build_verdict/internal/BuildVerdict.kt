package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.span.SpannedString

internal sealed class BuildVerdict {

    abstract val error: Error

    data class Configuration(override val error: Error) : BuildVerdict()

    data class Execution(
        override val error: Error,
        val failedTasks: List<FailedTask>
    ) : BuildVerdict()
}

internal data class FailedTask(
    val name: String,
    val projectPath: String,
    val errorLogs: String,
    val error: Error,
    val verdict: SpannedString? = null
)
