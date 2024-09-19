package com.avito.android.plugin.build_metrics.internal.gradle.cache

/**
 * Research http://links.k.avito.ru/i6v
 */
internal sealed interface BuildCacheMetricType {
    data class TaskType(val name: String) : BuildCacheMetricType
}
