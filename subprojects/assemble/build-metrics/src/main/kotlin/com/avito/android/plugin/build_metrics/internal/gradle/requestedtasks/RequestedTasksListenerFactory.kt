package com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultProvider
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.logger.LoggerFactory
import com.avito.utils.ProcessRunner
import java.time.Duration

internal class RequestedTasksListenerFactory(
    private val sender: BuildMetricSender,
    private val loggerFactory: LoggerFactory,
) {

    fun create(params: BuildOperationsResultProvider.Params): RequestedTasksListener {
        val projectDir = params.projectDir.get().asFile
        val processRunner = ProcessRunner.create(workingDirectory = projectDir)
        val logger = loggerFactory.create("RequestedTasksMetrics")
        val commitHash = lazy {
            processRunner.run("git rev-parse HEAD", GIT_TIMEOUT)
                .fold(
                    onSuccess = { it.trim() },
                    onFailure = {
                        logger.warn("Failed to get git commit hash", it)
                        "unknown"
                    }
                )
        }
        return RequestedTasksListener(
            sender = sender,
            requestedTasks = params.requestedTasks.get(),
            bootstrapRequestedTaskNames = params.bootstrapRequestedTaskNames.get(),
            metadata = RequestedTasksMetadata(
                userName = params.userName.get(),
                commitHashProvider = commitHash,
                isInvokedFromIde = params.invokedFromIde.get(),
            ),
            executionHistory = BuildExecutionHistory(
                historyFile = params.executionHistoryFile.get().asFile,
                loggerFactory = loggerFactory,
            ),
            hardwareInfoProvider = HardwareInfoProvider(),
        )
    }

    companion object {
        private val GIT_TIMEOUT: Duration = Duration.ofSeconds(5)
    }
}
