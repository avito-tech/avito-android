package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.metric.BuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import org.gradle.BuildResult
import org.gradle.api.Task

internal class LazyBuildEventsListener(
    private val listener: Lazy<BuildEventsListener>
) : AbstractBuildEventsListener() {
    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        listener.value.buildFinished(buildResult, profile)
    }

    override fun beforeExecute(task: Task) {
        listener.value.beforeExecute(task)
    }

    override fun afterExecute(task: Task, state: TaskExecution) {
        listener.value.afterExecute(task, state)
    }
}
