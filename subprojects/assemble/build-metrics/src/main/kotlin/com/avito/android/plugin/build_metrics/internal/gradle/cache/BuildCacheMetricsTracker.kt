package com.avito.android.plugin.build_metrics.internal.gradle.cache

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.CacheOperations
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult.Hit
import com.avito.android.plugin.build_metrics.internal.TaskCacheResult.Miss
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.android.plugin.build_metrics.internal.toTagValue
import com.avito.logger.LoggerFactory
import com.avito.logger.create

internal class BuildCacheMetricsTracker(
    private val metricsTracker: BuildMetricSender,
    private val observableTaskTypes: Set<String>,
    loggerFactory: LoggerFactory,
) : BuildOperationsResultListener {

    private val logger = loggerFactory.create<BuildCacheMetricsTracker>()
    override val name: String = "BuildCacheMetrics"

    override fun onBuildFinished(result: BuildOperationsResult) {
        trackCacheErrors(result.cacheOperations)
        trackRemoteCacheStats(result.tasksExecutions)
        trackTaskTypesCacheMetrics(result.tasksExecutions)
        trackModuleCacheMetrics(result.tasksExecutions)
    }

    private fun trackCacheErrors(operations: CacheOperations) {
        operations.errors.groupBy { it.selector }.forEach { (groupSelector, errors) ->
            metricsTracker.send(BuildCacheErrorMetric(groupSelector, errors.size.toLong()))
            if (groupSelector.httpStatus == null) {
                errors.forEach { error ->
                    // TODO handle httpStatus null
                    logger.warn("Unknown cache ${error.selector.type} error", error.cause)
                }
            }
        }
    }

    private fun trackRemoteCacheStats(tasksExecutions: List<TaskExecutionResult>) {
        val remoteHits = tasksExecutions
            .count { it.cacheResult is Hit.Remote }
            .toLong()

        val remoteMisses = tasksExecutions
            .count { it.cacheResult is Miss && it.cacheResult.remote }
            .toLong()

        metricsTracker.send(
            BuildCacheHitMetric(remoteHits)
        )
        metricsTracker.send(
            BuildCacheMissMetric(remoteMisses)
        )
    }

    private fun trackTaskTypesCacheMetrics(tasksExecutions: List<TaskExecutionResult>) {
        val taskTypesCacheResults = tasksExecutions.groupBy {
            it.type.simpleName
        }

        observableTaskTypes.forEach { taskType ->
            val taskTypeCacheResults = taskTypesCacheResults[taskType]

            if (taskTypeCacheResults != null) {
                trackTaskTypeCacheMetrics(taskTypeCacheResults, taskType)
                trackModuleTaskTypeCacheMetrics(taskTypeCacheResults, taskType)
            } else {
                // TODO send can't find data about task
            }
        }
    }

    private fun trackModuleTaskTypeCacheMetrics(
        taskTypeCacheResults: List<TaskExecutionResult>,
        taskType: String
    ) {
        val taskTypeCacheResultsByModule = taskTypeCacheResults.groupBy { it.path.module }
        taskTypeCacheResultsByModule.forEach { (module, cacheResults) ->
            val taskTypeModuleHit = cacheResults.count { it.cacheResult is Hit }
            val taskTypeModuleMiss = cacheResults.count { it.cacheResult is Miss && it.cacheResult.remote }
            metricsTracker.send(
                BuildCacheModuleTaskHitMetric(
                    type = BuildCacheMetricType.ModuleTaskType(
                        moduleName = module.toTagValue(),
                        taskType = taskType
                    ),
                    hitsCount = taskTypeModuleHit.toLong()
                )
            )

            metricsTracker.send(
                BuildCacheModuleTaskMissMetric(
                    type = BuildCacheMetricType.ModuleTaskType(
                        moduleName = module.toTagValue(),
                        taskType = taskType
                    ),
                    missesCount = taskTypeModuleMiss.toLong()
                )
            )
        }
    }

    private fun trackTaskTypeCacheMetrics(
        taskTypeCacheResults: List<TaskExecutionResult>,
        taskType: String
    ) {
        val taskTypeHit = taskTypeCacheResults.count { it.cacheResult is Hit }
        val taskTypeMiss = taskTypeCacheResults.count { it.cacheResult is Miss && it.cacheResult.remote }
        val type = BuildCacheMetricType.TaskType(taskType)
        metricsTracker.send(
            BuildCacheTaskHitMetric(
                type = type,
                hitsCount = taskTypeHit.toLong()
            )
        )
        metricsTracker.send(
            BuildCacheTaskMissMetric(
                type = type,
                missesCount = taskTypeMiss.toLong()
            )
        )
    }

    private fun trackModuleCacheMetrics(
        tasksExecutions: List<TaskExecutionResult>,
    ) {
        val taskExecutionsByModule = tasksExecutions.groupBy { it.path.module }
        taskExecutionsByModule.forEach { (module, taskExecutionResults) ->
            val moduleHit = taskExecutionResults
                .count { it.cacheResult is Hit.Remote }
                .toLong()

            val moduleMiss = taskExecutionResults
                .count { it.cacheResult is Miss && it.cacheResult.remote }
                .toLong()
            metricsTracker.send(
                BuildCacheModuleHitMetric(
                    type = BuildCacheMetricType.Module(module.toTagValue()),
                    hitsCount = moduleHit
                )
            )

            metricsTracker.send(
                BuildCacheModuleMissMetric(
                    type = BuildCacheMetricType.Module(module.toTagValue()),
                    missesCount = moduleMiss
                )
            )
        }
    }
}
