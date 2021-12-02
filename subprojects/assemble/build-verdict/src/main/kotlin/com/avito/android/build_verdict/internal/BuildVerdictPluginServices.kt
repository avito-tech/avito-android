package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.BuildVerdictPluginExtension
import com.avito.android.build_verdict.internal.task.lifecycle.BuildVerdictTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.DefaultTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.TaskExecutionListenerBridge
import com.avito.android.build_verdict.internal.task.lifecycle.TestTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.UserDefinedVerdictProducerTaskLifecycleListener
import com.avito.android.build_verdict.internal.writer.HtmlBuildVerdictWriter
import com.avito.android.build_verdict.internal.writer.PlainTextBuildVerdictWriter
import com.avito.android.build_verdict.internal.writer.RawBuildVerdictWriter
import com.avito.android.build_verdict.span.SpannedStringBuilder
import com.google.gson.GsonBuilder
import org.gradle.BuildListener
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.util.Path
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap

internal class BuildVerdictPluginServices(
    private val extension: BuildVerdictPluginExtension,
    private val logger: Logger
) {

    private val listeners = ConcurrentHashMap<OperationIdentifier, LogMessageListener>()
    private val logs = ConcurrentHashMap<Path, LogsTextBuilder>()
    private val verdicts = ConcurrentHashMap<Path, SpannedStringBuilder>()
    private val outputDir = lazy {
        extension.outputDir.get().asFile.apply { mkdirs() }
    }
    private val gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
    }

    private val buildFailedListener by lazy {
        BuildFailedListener(
            writers = listOf(
                RawBuildVerdictWriter(
                    gson = gson,
                    buildVerdictDir = outputDir,
                    logger = logger
                ),
                PlainTextBuildVerdictWriter(
                    buildVerdictDir = outputDir,
                    logger = logger
                ),
                HtmlBuildVerdictWriter(
                    buildVerdictDir = outputDir,
                    logger = logger
                )
            )
        )
    }

    fun gradleLogEventListener(): OutputEventListener {
        return GradleLogEventListener(
            listeners = listeners
        )
    }

    fun gradleTaskExecutionListener(): TaskExecutionListener {
        return TaskExecutionListenerBridge(
            listeners = listOf(
                TestTaskLifecycleListener(verdicts),
                BuildVerdictTaskLifecycleListener(verdicts),
                DefaultTaskLifecycleListener(logs, listeners),
                UserDefinedVerdictProducerTaskLifecycleListener(
                    taskVerdictProducers = lazy {
                        extension.taskVerdictProviders.getOrElse(
                            emptyList()
                        )
                    },
                    verdicts = verdicts
                )
            )
        )
    }

    fun gradleConfigurationListener() = BuildConfigurationFailureListener(buildFailedListener)

    fun gradleBuildFinishedListener(
        graph: TaskExecutionGraph
    ): BuildListener = BuildExecutionFailureListener(
        graph = graph,
        logs = logs,
        verdicts = verdicts,
        listener = buildFailedListener
    )
}
