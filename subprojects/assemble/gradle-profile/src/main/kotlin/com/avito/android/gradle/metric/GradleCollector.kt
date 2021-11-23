package com.avito.android.gradle.metric

import com.avito.android.gradle.metric.GradleCollector.Companion.initialize
import com.avito.android.gradle.profile.BuildListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.ProfileEventAdapter
import com.avito.android.gradle.profile.TaskExecution
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.internal.buildevents.BuildStartedTime
import org.gradle.internal.time.Clock
import org.gradle.kotlin.dsl.support.serviceOf

/**
 * Use [initialize] to consume events
 */
public class GradleCollector(
    // collection is used to avoid cyclic dependencies
    private val consumers: List<BuildEventsListener>,
) : BuildListener {

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

    private fun cleanup(gradle: Gradle) {
        gradle.removeListener(this)
    }

    public companion object {

        public fun initialize(project: Project, consumers: List<BuildEventsListener>) {
            if (consumers.isEmpty()) return

            val collector = GradleCollector(consumers)

            val gradle = project.gradle
            val clock = gradle.serviceOf<Clock>()
            val startedTime = gradle.serviceOf<BuildStartedTime>()
            val adapter = ProfileEventAdapter(clock, collector)
            adapter.buildStarted(startedTime)

            gradle.addListener(adapter)
        }
    }
}
