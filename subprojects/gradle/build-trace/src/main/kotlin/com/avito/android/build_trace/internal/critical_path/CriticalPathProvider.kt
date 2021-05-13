package com.avito.android.build_trace.internal.critical_path

internal interface CriticalPathProvider {
    fun path(): List<TaskOperation>
}
