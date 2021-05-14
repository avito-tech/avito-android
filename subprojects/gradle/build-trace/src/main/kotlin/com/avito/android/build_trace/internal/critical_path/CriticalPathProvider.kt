package com.avito.android.build_trace.internal.critical_path

import com.avito.graph.OperationsPath

internal interface CriticalPathProvider {
    fun path(): OperationsPath<TaskOperation>
}
