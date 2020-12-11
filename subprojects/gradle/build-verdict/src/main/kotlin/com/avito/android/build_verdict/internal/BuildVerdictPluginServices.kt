package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.BuildVerdictPluginExtension
import com.avito.android.build_verdict.internal.task.lifecycle.BuildVerdictTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.DefaultTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.TaskExecutionListenerBridge
import com.avito.android.build_verdict.internal.task.lifecycle.TestTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.UserDefinedVerdictProducerTaskLifecycleListener
import com.avito.android.build_verdict.internal.writer.CompositeBuildVerdictWriter
import com.avito.android.build_verdict.internal.writer.PlainTextBuildVerdictWriter
import com.avito.android.build_verdict.internal.writer.RawBuildVerdictWriter
import com.avito.utils.logging.CILogger
import com.google.gson.GsonBuilder
import org.gradle.BuildListener
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.internal.logging.events.OutputEventListener
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.util.Path
import java.util.concurrent.ConcurrentHashMap

internal class BuildVerdictPluginServices(
    private val extension: BuildVerdictPluginExtension,
    private val logger: CILogger
) {

    private val listeners = ConcurrentHashMap<OperationIdentifier, LogMessageListener>()
    private val logs = ConcurrentHashMap<Path, LogsTextBuilder>()
    private val verdicts = ConcurrentHashMap<Path, LogsTextBuilder>()
    private val gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
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

    fun gradleConfigurationListener(): BuildListener {
        return BuildConfigurationFailureListener(
            createWriter(
                outputDir = extension.outputDir,
                logger = logger
            )
        )
    }

    fun gradleBuildFinishedListener(
        graph: TaskExecutionGraph
    ): BuildListener = BuildExecutionFailureListener(
        graph = graph,
        logs = logs,
        verdicts = verdicts,
        writer = createWriter(extension.outputDir, logger)
    )

    private fun createWriter(
        outputDir: Provider<Directory>,
        logger: CILogger
    ): CompositeBuildVerdictWriter {
        return CompositeBuildVerdictWriter(
            writers = listOf(
                RawBuildVerdictWriter(
                    buildVerdictDir = outputDir,
                    logger = logger,
                    gson = gson
                ),
                PlainTextBuildVerdictWriter(
                    buildVerdictDir = outputDir,
                    logger = logger
                )
            )
        )
    }
}
