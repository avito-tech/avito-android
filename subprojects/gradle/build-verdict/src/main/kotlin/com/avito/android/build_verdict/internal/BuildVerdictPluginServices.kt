package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.internal.task.lifecycle.BuildVerdictTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.DefaultTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.TestTaskLifecycleListener
import com.avito.android.build_verdict.internal.writer.CompositeBuildVerdictWriter
import com.avito.android.build_verdict.internal.writer.PlainTextBuildVerdictWriter
import com.avito.android.build_verdict.internal.writer.RawBuildVerdictWriter
import com.avito.utils.logging.CILogger
import com.google.gson.GsonBuilder
import org.gradle.BuildResult
import org.gradle.api.Action
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.util.Path
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal class BuildVerdictPluginServices {
    private val listeners = ConcurrentHashMap<OperationIdentifier, LogMessageListener>()
    private val logs = ConcurrentHashMap<Path, LogsTextBuilder>()

    fun gradleLogEventListener(): OutputEventListener {
        return GradleLogEventListener(
            listeners = listeners
        )
    }

    fun gradleTaskExecutionListener(): TaskExecutionListener {
        return TaskExecutionErrorsCapture(
            testLifecycle = TestTaskLifecycleListener(logs),
            buildVerdictLifecycle = BuildVerdictTaskLifecycleListener(logs),
            defaultLifecycle = DefaultTaskLifecycleListener(logs, listeners)
        )
    }

    fun gradleBuildFinishedListener(
        graph: TaskExecutionGraph,
        outputDir: File,
        logger: CILogger
    ): Action<BuildResult> {
        return BuildFailureListener(
            graph = graph,
            logs = logs,
            writer = CompositeBuildVerdictWriter(
                writers = listOf(
                    RawBuildVerdictWriter(
                        buildVerdictDir = outputDir,
                        logger = logger,
                        gson = GsonBuilder()
                            .disableHtmlEscaping()
                            .setPrettyPrinting()
                            .create()
                    ),
                    PlainTextBuildVerdictWriter(
                        buildVerdictDir = outputDir,
                        logger = logger
                    )
                )
            )
        )
    }
}
