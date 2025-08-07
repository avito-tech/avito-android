package com.avito.android.plugin.build_metrics.internal

import com.avito.android.clickstream.config.ClickStreamConfig
import com.avito.android.graphite.GraphiteConfig
import com.avito.android.plugin.build_metrics.BuildEnvironment
import com.avito.android.plugin.build_metrics.internal.di.CompatibleWithConfigurationCacheDI
import com.avito.android.plugin.build_metrics.internal.result.BuildResult
import com.avito.android.plugin.build_metrics.internal.result.BuildStatus
import com.avito.android.stats.StatsDConfig
import com.avito.logger.GradleLoggerCoordinates
import com.avito.logger.LoggerService
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.flow.BuildWorkResult
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationDetails
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.caching.internal.controller.operations.LoadOperationDetails
import org.gradle.caching.internal.controller.operations.StoreOperationDetails
import org.gradle.caching.internal.operations.BuildCacheRemoteLoadBuildOperationType
import org.gradle.execution.RunRootBuildWorkBuildOperationType
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent
import org.gradle.internal.taskgraph.CalculateTreeTaskGraphBuildOperationType
import org.gradle.util.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Suppress("UnstableApiUsage")
internal abstract class BuildOperationsResultProvider : BuildService<BuildOperationsResultProvider.Params>,
    BuildOperationListener,
    AutoCloseable {

    interface Params : BuildServiceParameters {
        val test: Property<Boolean>
        val buildType: Property<String>
        val userName: Property<String>
        val environment: Property<BuildEnvironment>
        val statsdConfig: Property<StatsDConfig>
        val graphiteConfig: Property<GraphiteConfig>
        val clickStreamConfig: Property<ClickStreamConfig>
        val sendCompileMetrics: Property<Boolean>
        val compileMetricsMinimumDuration: Property<Duration>
        val sendSlowTaskMetrics: Property<Boolean>
        val slowTaskMinimumDuration: Property<Duration>
        val sendBuildCacheMetrics: Property<Boolean>
        val canTrackRemoteCache: Property<Boolean>
        val buildCacheObservableTasks: SetProperty<String>
        val writeModulesBuildTime: Property<Boolean>
        val modulesBuildTimeFile: RegularFileProperty
        val sendJvmMetrics: Property<Boolean>
        val sendOsMetrics: Property<Boolean>
        val sendBuildInitConfiguration: Property<Boolean>
        val sendBuildTotal: Property<Boolean>
        val sendAppBuildTime: Property<Boolean>
        val loggerService: Property<LoggerService>
        val loggerCoordinates: Property<GradleLoggerCoordinates>
    }

    private val di by lazy { CompatibleWithConfigurationCacheDI(parameters) }

    private val buildResultListeners by lazy {
        di.createBuildResultListener()
    }

    private val buildOperationsResultListener: BuildOperationsResultListener by lazy {
        di.createListener()
    }

    private val remoteLoadsByParentId: MutableMap<OperationIdentifier, BuildCacheRemoteLoadBuildOperationType.Result> =
        ConcurrentHashMap()
    private val tasksExecutionsById: MutableMap<OperationIdentifier, TaskExecutionIntermediateResult> =
        ConcurrentHashMap()
    private val buildCacheErrors = mutableListOf<RemoteBuildCacheError>()

    private lateinit var buildResult: BuildWorkResult

    private var startTime: Instant = Instant.now()
    private var configurationEndTime: Instant = Instant.now()

    override fun started(buildOperation: BuildOperationDescriptor, startEvent: OperationStartEvent) {
        // no-op
    }

    override fun progress(operationIdentifier: OperationIdentifier, progressEvent: OperationProgressEvent) {
        // no-op
    }

    override fun finished(descriptor: BuildOperationDescriptor, event: OperationFinishEvent) {
        val details = descriptor.details
        val result = event.result
        val failure = event.failure

        when {
            details is RunRootBuildWorkBuildOperationType.Details ->
                startTime = Instant.ofEpochMilli(details.buildStartTime)

            details is CalculateTreeTaskGraphBuildOperationType.Details ->
                configurationEndTime = Instant.ofEpochMilli(event.endTime)

            result is BuildCacheRemoteLoadBuildOperationType.Result -> onCacheRemoteLoad(descriptor, result)
            result is ExecuteTaskBuildOperationType.Result -> onTaskExecuted(descriptor, event, result)
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
            name = details.task.taskIdentity.name,
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

    internal fun onBuildResult(buildResult: BuildWorkResult) {
        this.buildResult = buildResult
    }

    override fun close() {
        val buildResult = BuildResult(
            status = if (buildResult.failure.isPresent) {
                BuildStatus.Fail
            } else {
                BuildStatus.Success
            },
            startTime = startTime,
            configurationEndTime = configurationEndTime,
            finishTime = Instant.now(),
        )
        val operationsResult = BuildOperationsResult(
            tasksExecutions = collectTasksExecutions(),
            cacheOperations = CacheOperations(
                errors = buildCacheErrors,
            ),
            buildResult = buildResult,
        )
        buildResultListeners.forEach {
            it.onBuildFinished(buildResult)
        }
        buildOperationsResultListener.onBuildFinished(operationsResult)
    }

    private fun collectTasksExecutions(): List<TaskExecutionResult> {
        return tasksExecutionsById
            .map { (taskId, intermediateResult) ->
                val cacheResult = determineTaskCacheResult(taskId, intermediateResult.result)

                TaskExecutionResult(
                    name = intermediateResult.name,
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
            selector = RemoteBuildCacheError.Selector(
                type = BuildCacheOperationType.LOAD,
                httpStatus = extractCacheLoadFailureStatusCode(cause),
            ),
            cause = cause
        )
        return buildCacheErrors.add(error)
    }

    private fun onBuildCacheStoreError(cause: Throwable): Boolean {
        val error = RemoteBuildCacheError(
            selector = RemoteBuildCacheError.Selector(
                type = BuildCacheOperationType.STORE,
                httpStatus = extractCacheStoreFailureStatusCode(cause),
            ),
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

    companion object {

        fun canTrackRemoteCache(project: Project): Boolean {
            val remoteBuildCache = (project as ProjectInternal).gradle.settings.buildCache.remote
            return remoteBuildCache != null
                && remoteBuildCache.isEnabled
        }
    }
}

private data class TaskExecutionIntermediateResult(
    val name: String,
    val path: String,
    val type: Class<out Task>,
    val startMs: Long,
    val endMs: Long,
    val result: ExecuteTaskBuildOperationType.Result
)

private val ExecuteTaskBuildOperationType.Result.isFromCache: Boolean
    get() = skipMessage == TaskExecutionOutcome.FROM_CACHE.message
