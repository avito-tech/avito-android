package com.avito.android.build_verdict.internal.task.lifecycle

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.android.build_verdict.span.SpannedStringBuilder
import org.gradle.api.Task
import org.gradle.util.Path

internal class BuildVerdictTaskLifecycleListener(
    private val verdicts: MutableMap<Path, SpannedStringBuilder>
) : TaskLifecycleListener<BuildVerdictTask>() {

    override val acceptedTask: Class<in BuildVerdictTask> = BuildVerdictTask::class.java

    override fun afterFailedExecute(task: BuildVerdictTask, error: Throwable) {
        verdicts.getOrPut(Path.path((task as Task).path), { SpannedStringBuilder() })
            .addLine(task.verdict)
    }
}
