package com.avito.android.plugin.build_metrics.internal.gradle.tasks.slow

internal sealed interface SlowMetricType {
    data class Module(val name: String) : SlowMetricType
    data class TaskType(val name: String) : SlowMetricType
    data class ModuleTaskType(
        val moduleName: String,
        val taskType: String,
    ) : SlowMetricType
}
