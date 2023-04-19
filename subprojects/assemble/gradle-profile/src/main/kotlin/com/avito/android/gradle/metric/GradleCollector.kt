package com.avito.android.gradle.metric

import com.avito.android.gradle.metric.GradleCollector.Companion.initialize
import com.avito.android.gradle.profile.BuildListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.gradle.profile.ProfileEventAdapter
import com.avito.android.gradle.profile.TaskExecution
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.Logger
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.internal.buildevents.BuildStartedTime
import org.gradle.internal.time.Clock
import org.gradle.kotlin.dsl.support.serviceOf
import java.time.Instant

/**
 * Use [initialize] to consume events
 */
public class GradleCollector(
    // collection is used to avoid cyclic dependencies
    private val consumers: List<BuildEventsListener>,
    private val logger: Logger,
) : BuildListener {

    override fun beforeExecute(task: Task) {
        consumers.forEach { it.beforeExecute(task) }
    }

    override fun afterExecute(task: Task, state: TaskExecution) {
        consumers.forEach { it.afterExecute(task, state) }
    }

    override fun buildFinished(result: BuildResult, profile: BuildProfile) {
        logger.info("Start build finished ${Instant.now()}")
        consumers.forEach {
            it.buildFinished(result, profile)
        }
        result.gradle?.also { cleanup(it) }
        logger.info("End build finished ${Instant.now()}")
    }

    private fun cleanup(gradle: Gradle) {
        gradle.removeListener(this)
    }

    public companion object {

        public fun initialize(name: String, project: Project, consumers: List<BuildEventsListener>) {
            if (consumers.isEmpty()) return
            val logger = GradleLoggerPlugin.getLoggerFactory(project).create(name)
            val collector = GradleCollector(consumers, logger)

            val gradle = project.gradle
            val clock = gradle.serviceOf<Clock>()
            val startedTime = gradle.serviceOf<BuildStartedTime>()
            val adapter = ProfileEventAdapter(clock, collector)
            adapter.buildStarted(startedTime)

            gradle.addListener(adapter)
        }
    }
}
