package com.avito.android.plugin.build_metrics.internal

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.gradle.metric.BuildEventsListener
import com.avito.android.plugin.build_metrics.internal.cache.BuildCacheMetricsTracker
import com.avito.android.plugin.build_metrics.internal.tasks.SlowTasksMetricsTracker
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
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.BuildOperationMetadata
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent
import org.gradle.util.Path
import java.util.concurrent.ConcurrentHashMap

internal class BuildOperationsResultProvider(
    private val resultListener: BuildOperationsResultListener
) : BuildOperationListener {

    private val remoteLoadsByParentId: MutableMap<OperationIdentifier, BuildCacheRemoteLoadBuildOperationType.Result> =
        ConcurrentHashMap()
    private val tasksExecutionsById: MutableMap<OperationIdentifier, TaskExecutionIntermediateResult> =
        ConcurrentHashMap()
    private val buildCacheErrors = mutableListOf<RemoteBuildCacheError>()

    override fun started(buildOperation: BuildOperationDescriptor, startEvent: OperationStartEvent) {
        // no-op
    }

    override fun progress(operationIdentifier: OperationIdentifier, progressEvent: OperationProgressEvent) {
        // no-op
    }

    override fun finished(descriptor: BuildOperationDescriptor, event: OperationFinishEvent) {
        val result = event.result
        val failure = event.failure

        when {
            result is BuildCacheRemoteLoadBuildOperationType.Result -> onCacheRemoteLoad(descriptor, result)
            result is ExecuteTaskBuildOperationType.Result -> onTaskExecuted(descriptor, event, result)
            descriptor.isRunTasksOperation() -> onRunTasks()
            failure != null && descriptor.details is LoadOperationDetails -> onBuildCacheLoadError(failure)
            failure != null && descriptor.details is StoreOperationDetails -> onBuildCacheStoreError(failure)
        }
    }

    private fun onTaskExecuted(
        descriptor: BuildOperationDescriptor,
        event: OperationFinishEvent,
        result: ExecuteTaskBuildOperationType.Result
    ) {
        val details = descriptor.details as ExecuteTaskBuildOperationDetails

        tasksExecutionsById[descriptor.id!!] = TaskExecutionIntermediateResult(
            path = details.taskPath,
            type = details.task.taskIdentity.type,
            startMs = event.startTime,
            endMs = event.endTime,
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
        val result = BuildOperationsResult(
            tasksExecutions = collectTasksExecutions(),
            cacheOperations = CacheOperations(
                errors = buildCacheErrors,
            )
        )
        resultListener.onBuildFinished(result)
    }

    private fun collectTasksExecutions(): List<TaskExecutionResult> {
        return tasksExecutionsById
            .map { (taskId, intermediateResult) ->
                val cacheResult = determineTaskCacheResult(taskId, intermediateResult.result)

                TaskExecutionResult(
                    path = Path.path(intermediateResult.path),
                    type = intermediateResult.type,
                    startMs = intermediateResult.startMs,
                    endMs = intermediateResult.endMs,
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

    private fun onBuildCacheLoadError(cause: Throwable): Boolean {
        val error = RemoteBuildCacheError(
            type = BuildCacheOperationType.LOAD,
            httpStatus = extractCacheLoadFailureStatusCode(cause),
            cause = cause
        )
        return buildCacheErrors.add(error)
    }

    private fun onBuildCacheStoreError(cause: Throwable): Boolean {
        val error = RemoteBuildCacheError(
            type = BuildCacheOperationType.STORE,
            httpStatus = extractCacheStoreFailureStatusCode(cause),
            cause = cause
        )
        return buildCacheErrors.add(error)
    }

    /**
     * Sample:
     * Loading entry from 'http://host/cache/abcdef' response status 500: Server Error
     */
    private fun extractCacheLoadFailureStatusCode(error: Throwable): Int? {
        val message = error.message ?: return null

        return if (message.startsWith("Loading entry from")) {
            message.substringAfter(" response status ").substringBefore(':').trim()
                .toInt()
        } else {
            null
        }
    }

    /**
     * Sample:
     * Storing entry at 'http://host/cache/abcdef' response status 500: Server Error
     */
    private fun extractCacheStoreFailureStatusCode(error: Throwable): Int? {
        val message = error.message ?: return null

        return if (message.startsWith("Storing entry at")) {
            message.substringAfter(" response status ").substringBefore(':').trim()
                .toInt()
        } else {
            null
        }
    }

    private fun BuildOperationDescriptor.isRunTasksOperation(): Boolean {
        return details is RunRootBuildWorkBuildOperationType.Details
            && (metadata == BuildOperationCategory.RUN_WORK || metadata.isRunTaskGradle6())
    }

    private fun BuildOperationMetadata.isRunTaskGradle6(): Boolean {
        return (this as? BuildOperationCategory)?.name == "RUN_WORK_ROOT_BUILD"
    }

    companion object {

        fun register(
            project: Project,
            metricsTracker: BuildMetricTracker
        ): BuildEventsListener {
            val listeners = mutableListOf<BuildOperationsResultListener>()
            if (canTrackRemoteCache(project)) {
                listeners.add(
                    BuildCacheMetricsTracker(
                        metricsTracker = metricsTracker,
                        logger = project.logger
                    )
                )
            }
            listeners.add(
                SlowTasksMetricsTracker(
                    metricsTracker = metricsTracker,
                )
            )
            val buildOperationListener = BuildOperationsResultProvider(
                resultListener = CompositeBuildOperationsResultListener(listeners),
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
    val startMs: Long,
    val endMs: Long,
    val result: ExecuteTaskBuildOperationType.Result
)

private val ExecuteTaskBuildOperationType.Result.isFromCache: Boolean
    get() = skipMessage == TaskExecutionOutcome.FROM_CACHE.message
