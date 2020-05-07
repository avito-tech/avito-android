package com.avito.android.gradle.metric

import com.avito.android.gradle.profile.BuildListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.ProfileEventAdapter
import com.avito.android.gradle.profile.TaskExecution
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.StandardOutputListener
import org.gradle.internal.buildevents.BuildStartedTime
import org.gradle.internal.logging.LoggingOutputInternal
import org.gradle.internal.time.Clock
import org.gradle.kotlin.dsl.support.serviceOf

/**
 * Use [initialize] to consume events
 */
class GradleCollector(
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

    companion object {

        fun initialize(project: Project, consumers: List<MetricsConsumer>) {
            if (consumers.isEmpty()) return

            val collector = GradleCollector(consumers)

            val gradle = project.gradle
            val clock = gradle.serviceOf<Clock>()
            val startedTime = gradle.serviceOf<BuildStartedTime>()
            val adapter = ProfileEventAdapter(clock, collector)
            adapter.buildStarted(startedTime)

            gradle.addListener(adapter)

            val outputService = gradle.serviceOf<LoggingOutputInternal>()
            outputService.addStandardOutputListener(collector)
        }
    }
}
