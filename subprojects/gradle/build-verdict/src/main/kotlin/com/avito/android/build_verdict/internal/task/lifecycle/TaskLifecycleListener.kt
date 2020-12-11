package com.avito.android.build_verdict.internal.task.lifecycle

import org.gradle.api.Task
import org.gradle.api.tasks.TaskState

internal abstract class TaskLifecycleListener<in T> {

    abstract val acceptedTask: Class<in T>

    @Suppress("UNCHECKED_CAST")
    fun beforeExecute(task: Task) {
        if (acceptedTask.isInstance(task)) {
            beforeExecuteTyped(task as T)
        }
    }

    protected open fun beforeExecuteTyped(task: T) {
        // empty
    }

    protected open fun afterSucceedExecute(task: T) {
        // empty
    }

    protected open fun afterFailedExecute(task: T, error: Throwable) {
        // empty
    }

    @Suppress("UNCHECKED_CAST")
    fun afterExecute(task: Task, state: TaskState) {
        if (acceptedTask.isInstance(task)) {
            val failure = state.failure
            if (failure != null) {
                afterFailedExecute(task as T, failure)
            } else {
                afterSucceedExecute(task as T)
            }
        }
    }
}
