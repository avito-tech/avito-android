package com.avito.android.gradle.metric

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import org.gradle.BuildResult
import org.gradle.api.Task

interface MetricsConsumer : com.avito.android.gradle.profile.TaskExecutionListener {

    fun onOutput(output: CharSequence)

    fun buildFinished(buildResult: BuildResult, profile: BuildProfile)
}

abstract class AbstractMetricsConsumer : MetricsConsumer {

    /**
     * Do not print or log in this method. It'll cause an infinite recursion.
     */
    override fun onOutput(output: CharSequence) {
    }

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
    }

    override fun beforeExecute(task: Task) {
    }

    override fun afterExecute(task: Task, state: TaskExecution) {
    }
}
