package com.avito.android.critical_path

import com.avito.graph.OperationsPath

/**
 * Use [CriticalPathRegistry.addListener] to register a listener
 */
public interface CriticalPathListener {

    public fun onCriticalPathReady(path: OperationsPath<TaskOperation>)
}
