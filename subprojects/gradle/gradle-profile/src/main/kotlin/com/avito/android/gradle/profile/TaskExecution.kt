package com.avito.android.gradle.profile

import org.gradle.api.tasks.TaskState

class TaskExecution(val path: String) : ContinuousOperation(path) {

    var state: TaskState? = null
        private set

    val status: String?
        get() = if (this.state!!.skipped) this.state!!.skipMessage else if (this.state!!.didWork) "" else "Did No Work"

    fun completed(state: TaskState): TaskExecution {
        this.state = state
        return this
    }
}
