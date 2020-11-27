package com.avito.android.build_verdict.internal.task.lifecycle

import com.avito.android.build_verdict.internal.LogsTextBuilder
import org.gradle.api.Task
import org.gradle.util.Path

abstract class TaskLifecycleListener<in T> {

    protected abstract val logs: MutableMap<Path, LogsTextBuilder>

    abstract fun beforeExecute(task: T)

    protected abstract fun afterSucceedExecute(task: T)

    protected abstract fun afterFailedExecute(task: T)

    fun afterExecute(task: T, failure: Throwable?) {
        if (failure == null) {
            logs.remove(Path.path((task as Task).path))
            afterSucceedExecute(task)
        } else {
            afterFailedExecute(task)
        }
    }
}
