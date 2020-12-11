package com.avito.android.build_verdict.internal

import com.avito.android.build_verdict.internal.task.lifecycle.BuildVerdictTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.DefaultTaskLifecycleListener
import com.avito.android.build_verdict.internal.task.lifecycle.TestTaskLifecycleListener
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

internal class BuildVerdictPluginServices {

    private val listeners = ConcurrentHashMap<OperationIdentifier, LogMessageListener>()
    private val logs = ConcurrentHashMap<Path, LogsTextBuilder>()
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
        return TaskExecutionErrorsCapture(
            testLifecycle = TestTaskLifecycleListener(logs),
            buildVerdictLifecycle = BuildVerdictTaskLifecycleListener(logs),
            defaultLifecycle = DefaultTaskLifecycleListener(logs, listeners)
        )
    }

    fun gradleConfigurationListener(
        outputDir: Provider<Directory>,
        logger: CILogger
    ): BuildListener {
        return BuildConfigurationFailureListener(
            createWriter(
                outputDir = outputDir,
                logger = logger
            )
        )
    }

    fun gradleBuildFinishedListener(
        graph: TaskExecutionGraph,
        outputDir: Provider<Directory>,
        logger: CILogger
    ): BuildListener = BuildExecutionFailureListener(
        graph = graph,
        logs = logs,
        writer = createWriter(outputDir, logger)
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
