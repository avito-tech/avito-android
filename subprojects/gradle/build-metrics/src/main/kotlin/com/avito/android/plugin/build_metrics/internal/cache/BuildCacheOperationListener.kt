package com.avito.android.plugin.build_metrics.internal.cache

import com.avito.android.gradle.metric.BuildEventsListener
import com.avito.android.gradle.metric.NoOpBuildEventsListener
import com.avito.android.plugin.build_metrics.BuildMetricsPlugin
import com.avito.android.plugin.build_metrics.internal.AbstractBuildOperationListener
import com.avito.android.plugin.build_metrics.internal.buildOperationListenerManager
import com.avito.android.plugin.build_metrics.internal.dump
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.statsd
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.Logger
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationDetails
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.caching.internal.controller.operations.LoadOperationDetails
import org.gradle.caching.internal.controller.operations.StoreOperationDetails
import org.gradle.caching.internal.operations.BuildCacheRemoteLoadBuildOperationType
import org.gradle.execution.RunRootBuildWorkBuildOperationType
import org.gradle.internal.operations.BuildOperationCategory
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import java.util.concurrent.ConcurrentHashMap

internal class BuildCacheOperationListener(
    private val eventsConsumer: BuildCacheEventsConsumer,
    private val logger: Logger
) : AbstractBuildOperationListener() {

    private val remoteLoadsByParentId: MutableMap<OperationIdentifier, BuildCacheRemoteLoadBuildOperationType.Result> =
        ConcurrentHashMap()
    private val tasksExecutionsById: MutableMap<OperationIdentifier, TaskExecutionIntermediateResult> =
        ConcurrentHashMap()

    override fun finished(descriptor: BuildOperationDescriptor, event: OperationFinishEvent) {
        val result = event.result
        val failure = event.failure

        when {
            result is BuildCacheRemoteLoadBuildOperationType.Result -> onCacheRemoteLoad(descriptor, result)
            result is ExecuteTaskBuildOperationType.Result -> onTaskExecuted(descriptor, result)
            descriptor.isRunTasksOperation() -> onRunTasks()
            failure != null && descriptor.details is LoadOperationDetails -> onCacheLoadError(failure)
            failure != null && descriptor.details is StoreOperationDetails -> onCacheStoreError(failure)
        }
    }

    private fun onTaskExecuted(descriptor: BuildOperationDescriptor, result: ExecuteTaskBuildOperationType.Result) {
        val details = descriptor.details as ExecuteTaskBuildOperationDetails

        tasksExecutionsById[descriptor.id!!] = TaskExecutionIntermediateResult(
            path = details.taskPath,
            type = details.task.taskIdentity.type,
            result = result
        )
    }

    private fun onCacheRemoteLoad(
        descriptor: BuildOperationDescriptor,
        result: BuildCacheRemoteLoadBuildOperationType.Result
    ) {
        val parentId = checkNotNull(descriptor.parentId) {
            "Unexpected state of ${descriptor.dump()}"
        }
        remoteLoadsByParentId[parentId] = result
    }

    private fun onRunTasks() {
        eventsConsumer.onBuildFinished(
            collectTasksExecutions()
        )
    }

    private fun collectTasksExecutions(): List<TaskExecutionResult> {
        return tasksExecutionsById
            .map { (taskId, intermediateResult) ->
                val cacheResult = determineTaskCacheResult(taskId, intermediateResult.result)

                TaskExecutionResult(
                    path = intermediateResult.path,
                    type = intermediateResult.type,
                    cacheResult = cacheResult,
                )
            }
    }

    private fun determineTaskCacheResult(
        taskId: OperationIdentifier,
        result: ExecuteTaskBuildOperationType.Result
    ): TaskCacheResult {
        val isTaskCacheDisabled = result.cachingDisabledReasonCategory != null

        return if (isTaskCacheDisabled) {
            TaskCacheResult.Disabled
        } else {
            val wasRemoteCacheLoad = remoteLoadsByParentId[taskId] != null

            if (result.isFromCache) {
                if (wasRemoteCacheLoad) TaskCacheResult.Hit.Remote else TaskCacheResult.Hit.Local
            } else {
                // TODO: consider disabled local cache
                TaskCacheResult.Miss(local = true, remote = wasRemoteCacheLoad)
            }
        }
    }

    private fun onCacheLoadError(failure: Throwable) {
        eventsConsumer.onCacheLoadError(
            extractLoadFailureStatusCode(failure)
        )
    }

    private fun onCacheStoreError(failure: Throwable) {
        eventsConsumer.onCacheStoreError(
            extractStoreFailureStatusCode(failure)
        )
    }

    private fun BuildOperationDescriptor.isRunTasksOperation(): Boolean {
        return details is RunRootBuildWorkBuildOperationType.Details
            && metadata == BuildOperationCategory.RUN_WORK_ROOT_BUILD
    }

    /**
     * Sample:
     * Loading entry from 'http://host/cache/abcdef' response status 500: Server Error
     */
    private fun extractLoadFailureStatusCode(error: Throwable): Int? {
        val message = error.message ?: return null

        return if (message.startsWith("Loading entry from")) {
            message.substringAfter(" response status ").substringBefore(':').trim()
                .toInt()
        } else {
            logger.warn("Unknown cache load error", error)
            null
        }
    }

    /**
     * Sample:
     * Storing entry at 'http://host/cache/abcdef' response status 500: Server Error
     */
    private fun extractStoreFailureStatusCode(error: Throwable): Int? {
        val message = error.message ?: return null

        return if (message.startsWith("Storing entry at")) {
            message.substringAfter(" response status ").substringBefore(':').trim()
                .toInt()
        } else {
            logger.warn("Unknown cache store error", error)
            null
        }
    }

    companion object {

        fun register(project: Project): BuildEventsListener {
            if (!canTrackRemoteCache(project)) return NoOpBuildEventsListener()

            val loggerFactory = GradleLoggerFactory.fromProject(project, BuildMetricsPlugin::class.java.simpleName)
            val logger = loggerFactory.create(BuildCacheOperationListener::class.java.simpleName)

            val eventsConsumer = BuildCacheEventsConsumerImpl(
                statsd = project.statsd,
                environmentInfo = project.environmentInfo(),
            )

            val buildOperationListener = BuildCacheOperationListener(
                eventsConsumer = eventsConsumer,
                logger = logger
            )
            project.gradle.buildOperationListenerManager().addListener(buildOperationListener)

            return BuildOperationListenerCleaner(buildOperationListener)
        }

        private fun canTrackRemoteCache(project: Project): Boolean {
            val remoteBuildCache = (project as ProjectInternal).gradle.settings.buildCache.remote
            return remoteBuildCache != null
                && remoteBuildCache.isEnabled
        }
    }
}

private data class TaskExecutionIntermediateResult(
    val path: String,
    val type: Class<out Task>,
    val result: ExecuteTaskBuildOperationType.Result
)

private val ExecuteTaskBuildOperationType.Result.isFromCache: Boolean
    get() = skipMessage == TaskExecutionOutcome.FROM_CACHE.message
