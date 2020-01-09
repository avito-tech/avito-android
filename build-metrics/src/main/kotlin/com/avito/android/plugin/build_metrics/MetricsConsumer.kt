package com.avito.android.plugin.build_metrics

import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import org.gradle.BuildResult
import org.gradle.api.Task

internal interface MetricsConsumer : com.avito.android.gradle.profile.TaskExecutionListener {

    fun onOutput(output: CharSequence)

    fun buildFinished(buildResult: BuildResult, profile: BuildProfile)

}

internal abstract class AbstractMetricsConsumer : MetricsConsumer {

    /**
     * Do not print or log in this method. It'll cause infinite recursion.
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
