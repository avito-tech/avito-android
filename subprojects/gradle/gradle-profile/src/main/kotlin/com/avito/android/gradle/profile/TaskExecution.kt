package com.avito.android.gradle.profile

import org.gradle.api.tasks.TaskState
import org.gradle.util.Path

public class TaskExecution(public val path: Path) : ContinuousOperation(path.path) {

    public var state: TaskState? = null
        private set

    public val name: String =
        path.path.substringAfterLast(':')

    public val module: Path =
        path.parent ?: Path.ROOT

    public val status: String?
        get() = if (this.state!!.skipped) this.state!!.skipMessage else if (this.state!!.didWork) "" else "Did No Work"

    public fun completed(state: TaskState): TaskExecution {
        this.state = state
        return this
    }
}
