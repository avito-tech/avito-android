package com.avito.android.plugin.build_metrics

import com.avito.android.gradle.profile.BuildListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.TaskExecution
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.StandardOutputListener
import org.gradle.internal.logging.LoggingOutputInternal
import org.gradle.kotlin.dsl.support.serviceOf

internal open class GradleCollector(
    // collection is used to avoid cyclic dependencies
    private val consumers: List<MetricsConsumer>
) : BuildListener, StandardOutputListener {

    override fun beforeExecute(task: Task) {
        consumers.forEach { it.beforeExecute(task) }
    }

    override fun afterExecute(task: Task, state: TaskExecution) {
        consumers.forEach { it.afterExecute(task, state) }
    }

    override fun buildFinished(result: BuildResult, profile: BuildProfile) {
        consumers.forEach {
            it.buildFinished(result, profile)
        }
        result.gradle?.also { cleanup(it) }
    }

    override fun onOutput(output: CharSequence?) {
        consumers.forEach {
            if (output != null) {
                it.onOutput(output)
            }
        }
    }

    private fun cleanup(gradle: Gradle) {
        gradle.removeListener(this)
        gradle.serviceOf<LoggingOutputInternal>().removeStandardOutputListener(this)
    }

}
