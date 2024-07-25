package com.avito.android.plugin.build_metrics.internal.gradle.cache

internal sealed interface BuildCacheMetricType {
    data class Module(val name: String) : BuildCacheMetricType
    data class TaskType(val name: String) : BuildCacheMetricType
    data class ModuleTaskType(
        val moduleName: String,
        val taskType: String,
    ) : BuildCacheMetricType
}
