package com.avito.android.gradle.metric

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import org.gradle.BuildResult
import org.gradle.api.Task

interface BuildEventsListener : com.avito.android.gradle.profile.TaskExecutionListener {

    fun buildFinished(buildResult: BuildResult, profile: BuildProfile)
}

abstract class AbstractBuildEventsListener : BuildEventsListener {

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
    }

    override fun beforeExecute(task: Task) {
    }

    override fun afterExecute(task: Task, state: TaskExecution) {
    }
}

class NoOpBuildEventsListener : AbstractBuildEventsListener()
