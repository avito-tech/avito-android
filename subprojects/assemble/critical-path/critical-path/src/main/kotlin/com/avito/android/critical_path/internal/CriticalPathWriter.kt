package com.avito.android.critical_path.internal

import com.avito.android.critical_path.CriticalPathListener
import com.avito.android.critical_path.TaskOperation
import com.avito.graph.OperationsPath

internal class CriticalPathWriter(
    private val report: CriticalPathReport
) : CriticalPathListener {

    override fun onCriticalPathReady(path: OperationsPath<TaskOperation>) {
        val records = path.operations
            .map {
                PathRecord.fromTaskOperation(it)
            }
        report.write(records)
    }
}
