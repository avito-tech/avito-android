package com.avito.android.gradle.profile

import org.gradle.api.tasks.TaskState
import org.gradle.util.Path

class TaskExecution(val path: Path) : ContinuousOperation(path.path) {

    var state: TaskState? = null
        private set

    val name: String =
        path.path.substringAfterLast(':')

    val module: Path =
        path.parent ?: Path.ROOT

    val status: String?
        get() = if (this.state!!.skipped) this.state!!.skipMessage else if (this.state!!.didWork) "" else "Did No Work"

    fun completed(state: TaskState): TaskExecution {
        this.state = state
        return this
    }
}
