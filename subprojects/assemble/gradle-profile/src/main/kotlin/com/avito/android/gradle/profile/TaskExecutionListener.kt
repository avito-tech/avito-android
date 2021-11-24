package com.avito.android.gradle.profile

import org.gradle.api.Task

/**
 * Replacement for [org.gradle.api.execution.TaskExecutionListener] with enriched task state.
 */
public interface TaskExecutionListener {
    /**
     * This method is called immediately before a task is executed.
     *
     * @param task The task about to be executed. Never null.
     */
    public fun beforeExecute(task: Task)

    /**
     * This method is call immediately after a task has been executed. It is always called, regardless of whether the
     * task completed successfully, or failed with an exception.
     *
     * @param task The task which was executed. Never null.
     * @param state The task state. If the task failed with an exception, the exception is available in this
     * state. Never null.
     */
    public fun afterExecute(task: Task, state: TaskExecution)
}
