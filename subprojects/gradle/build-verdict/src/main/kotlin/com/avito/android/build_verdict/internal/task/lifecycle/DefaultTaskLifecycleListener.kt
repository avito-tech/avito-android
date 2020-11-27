package com.avito.android.build_verdict.internal.task.lifecycle

import com.avito.android.build_verdict.internal.LogMessageListener
import com.avito.android.build_verdict.internal.LogsTextBuilder
import org.gradle.api.Task
import org.gradle.internal.operations.CurrentBuildOperationRef
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.util.Path

class DefaultTaskLifecycleListener(
    override val logs: MutableMap<Path, LogsTextBuilder>,
    private val listeners: MutableMap<OperationIdentifier, LogMessageListener>
) : TaskLifecycleListener<Task>() {

    override fun beforeExecute(task: Task) {
        val id = CurrentBuildOperationRef.instance().id
        if (id != null) {
            val path = Path.path(task.path)
            listeners.getOrPut(id) {
                ErrorOutputListener(
                    path = path,
                    logs = logs
                )
            }
        }
    }

    override fun afterSucceedExecute(task: Task) {
        deleteListener()
    }

    override fun afterFailedExecute(task: Task) {
        deleteListener()
    }

    private fun deleteListener() {
        val id = CurrentBuildOperationRef.instance().id
        if (id != null) {
            listeners.remove(id)
        }
    }

    private class ErrorOutputListener(
        private val path: Path,
        private val logs: MutableMap<Path, LogsTextBuilder>
    ) : LogMessageListener {

        override fun onLogMessage(message: String) {
            logs.getOrPut(path, { LogsTextBuilder() })
                .addLine(message)
        }
    }
}
