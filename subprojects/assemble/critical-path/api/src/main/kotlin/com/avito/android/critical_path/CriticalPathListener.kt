package com.avito.android.critical_path

import com.avito.graph.OperationsPath

/**
 * Use [CriticalPathRegistry.addListener] to register a listener
 */
public interface CriticalPathListener {

    /**
     * Will be invoked at the end of a build
     */
    public fun onCriticalPathReady(path: OperationsPath<TaskOperation>)
}
