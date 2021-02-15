package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.stats.SeriesName
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.util.Path

internal fun Path.toSeriesName(): SeriesName {
    return SeriesName.create(
        path.removePrefix(":").ifEmpty { "_" }
    )
}

internal val TaskExecution.internalState: TaskStateInternal
    get() = try {
        state as TaskStateInternal
    } catch (e: Exception) {
        error(
            "Task $path has unsupported class $javaClass"
        )
    }
