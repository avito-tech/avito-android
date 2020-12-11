package com.avito.android.build_verdict.internal.task.lifecycle

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.internal.LogsTextBuilder
import org.gradle.api.Task
import org.gradle.util.Path

internal class BuildVerdictTaskLifecycleListener(
    private val verdicts: MutableMap<Path, LogsTextBuilder>
) : TaskLifecycleListener<BuildVerdictTask>() {

    override val acceptedTask: Class<in BuildVerdictTask> = BuildVerdictTask::class.java

    override fun afterFailedExecute(task: BuildVerdictTask, error: Throwable) {
        verdicts.getOrPut(Path.path((task as Task).path), { LogsTextBuilder() })
            .addLine(task.verdict)
    }
}
