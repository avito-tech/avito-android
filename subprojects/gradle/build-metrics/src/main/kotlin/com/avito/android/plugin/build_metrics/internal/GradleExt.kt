package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.stats.SeriesName
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.invocation.Gradle
import org.gradle.internal.operations.BuildOperationListenerManager
import org.gradle.util.Path

internal fun Path.toSeriesName(): SeriesName {
    return SeriesName.create(
        path.removePrefix(":").ifEmpty { "_" }
    )
}

internal val Path.module: Path
    get() = parent ?: Path.ROOT

internal val TaskExecution.internalState: TaskStateInternal
    get() = try {
        state as TaskStateInternal
    } catch (e: Exception) {
        error(
            "Task $path has unsupported class $javaClass"
        )
    }

internal fun Gradle.buildOperationListenerManager(): BuildOperationListenerManager =
    (this as GradleInternal).services[BuildOperationListenerManager::class.java]
