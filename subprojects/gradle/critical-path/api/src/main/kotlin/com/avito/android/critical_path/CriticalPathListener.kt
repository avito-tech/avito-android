package com.avito.android.critical_path

import com.avito.graph.OperationsPath

public interface CriticalPathListener {

    public fun onCriticalPathReady(path: OperationsPath<TaskOperation>)
}
