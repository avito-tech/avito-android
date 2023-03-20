package com.avito.android.plugin.build_metrics.internal

import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.internal.operations.BuildOperationListenerManager
import org.gradle.util.Path

internal fun Path.toTagValue(): String {
    return if (Path.ROOT == this) {
        "root"
    } else {
        val tagValue = path.removePrefix(":").replace(":", "_")
        require(tagValue.isNotBlank()) {
            "Can't convert path: $this to tag value"
        }
        tagValue
    }
}

internal val Path.module: Path
    get() = parent ?: Path.ROOT

internal fun Gradle.buildOperationListenerManager(): BuildOperationListenerManager =
    (this as GradleInternal).services[BuildOperationListenerManager::class.java]
