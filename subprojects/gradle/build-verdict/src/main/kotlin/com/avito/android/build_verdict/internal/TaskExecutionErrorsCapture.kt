package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.internal.task.lifecycle.TaskLifecycleListener
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.Test

internal class TaskExecutionErrorsCapture(
    private val testLifecycle: TaskLifecycleListener<Test>,
    private val buildVerdictLifecycle: TaskLifecycleListener<BuildVerdictTask>,
    private val defaultLifecycle: TaskLifecycleListener<Task>
) : TaskExecutionListener, BuildAdapter() {

    override fun beforeExecute(task: Task) {
        when (task) {
            is Test -> testLifecycle.beforeExecute(task)
            is BuildVerdictTask -> buildVerdictLifecycle.beforeExecute(task)
            else -> defaultLifecycle.beforeExecute(task)
        }
    }

    override fun afterExecute(task: Task, state: TaskState) {
        when (task) {
            is Test -> testLifecycle.afterExecute(task, state.failure)
            is BuildVerdictTask -> buildVerdictLifecycle.afterExecute(task, state.failure)
            else -> defaultLifecycle.afterExecute(task, state.failure)
        }
    }

    override fun buildFinished(result: BuildResult) {
        result.gradle?.removeListener(this)
    }
}
