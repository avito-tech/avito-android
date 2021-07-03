package com.avito.android.gradle.metric

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import com.avito.android.gradle.profile.TaskExecutionListener
import org.gradle.BuildResult
import org.gradle.api.Task

public interface BuildEventsListener : TaskExecutionListener {

    public fun buildFinished(buildResult: BuildResult, profile: BuildProfile)
}

public abstract class AbstractBuildEventsListener : BuildEventsListener {

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
    }

    override fun beforeExecute(task: Task) {
    }

    override fun afterExecute(task: Task, state: TaskExecution) {
    }
}

internal class NoOpBuildEventsListener : AbstractBuildEventsListener()
