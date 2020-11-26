package com.avito.android.build_verdict.internal.task.lifecycle

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.internal.LogsTextBuilder
import org.gradle.api.Task
import org.gradle.util.Path

class BuildVerdictTaskLifecycleListener(
    override val logs: MutableMap<Path, LogsTextBuilder>
) : TaskLifecycleListener<BuildVerdictTask>() {

    override fun beforeExecute(task: BuildVerdictTask) {
        // empty
    }

    override fun afterFailedExecute(task: BuildVerdictTask) {
        logs.getOrPut(Path.path((task as Task).path), { LogsTextBuilder() })
            .addLine(task.verdict)
    }

    override fun afterSucceedExecute(task: BuildVerdictTask) {
        // empty
    }
}
