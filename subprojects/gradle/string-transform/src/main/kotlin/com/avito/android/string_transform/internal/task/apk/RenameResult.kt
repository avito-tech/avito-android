package com.avito.android.string_transform.internal.task.apk

internal data class RenameResult(
    val warnings: List<OperationWarning>,
    val pathMapping: Map<String, String>,
)
