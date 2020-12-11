package com.avito.android.build_verdict.internal.task.lifecycle

import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

internal class TaskExecutionListenerBridge(
    private val listeners: List<TaskLifecycleListener<*>>
) : TaskExecutionListener, BuildAdapter() {

    override fun beforeExecute(task: Task) {
        listeners.forEach { it.beforeExecute(task) }
    }

    override fun afterExecute(task: Task, state: TaskState) {
        listeners.forEach { it.afterExecute(task, state) }
    }

    override fun buildFinished(result: BuildResult) {
        result.gradle?.removeListener(this)
    }
}
