package com.avito.android.string_transform.internal.execution

import com.avito.android.Result
import com.avito.android.string_transform.internal.report.TransformReportRecorder

internal fun runTransform(
    recorder: TransformReportRecorder,
    block: TransformExecutionScope.() -> Unit,
): Result<Unit> {
    return Result.tryCatch {
        TransformExecutionScope(recorder).block()
    }
}

internal class TransformExecutionScope(
    private val recorder: TransformReportRecorder,
) {

    fun <T> step(name: String, action: () -> Result<T>): T {
        return recorder.recordPhase(name) {
            action()
        }.getOrThrow()
    }
}
