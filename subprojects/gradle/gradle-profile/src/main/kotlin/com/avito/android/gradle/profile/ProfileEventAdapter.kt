package com.avito.android.gradle.profile

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.internal.buildevents.BuildStartedTime
import org.gradle.internal.time.Clock

/**
 * This is a copy of original org.gradle.profile service.
 * See discussions in https://github.com/nebula-plugins/gradle-nebula-integration/issues/31
 *
 * We can't register it directly.
 * https://discuss.gradle.org/t/plugin-service-registration/29342
 *
 */
class ProfileEventAdapter(
    private val clock: Clock,
    private val listener: com.avito.android.gradle.profile.BuildListener
) : BuildListener, ProjectEvaluationListener, TaskExecutionListener {

    private var buildProfile: BuildProfile? = null

    fun buildStarted(startedTime: BuildStartedTime) {
        val now = clock.currentTime

        buildProfile = BuildProfile()
        buildProfile?.profilingStarted = startedTime.startTime
        buildProfile?.buildStarted = now
    }

    override fun settingsEvaluated(settings: Settings) {
        val now = clock.currentTime
        buildProfile!!.setSettingsEvaluated(now)
    }

    override fun projectsLoaded(gradle: Gradle) {
        val now = clock.currentTime
        buildProfile!!.setProjectsLoaded(now)
    }

    override fun projectsEvaluated(gradle: Gradle) {
        val now = clock.currentTime
        buildProfile!!.setProjectsEvaluated(now)
    }

    override fun buildFinished(result: BuildResult) {
        buildProfile!!.isSuccessful = result.failure == null

        // We complete profiling here because it's too late to remove Gradle listeners
        // in org.gradle.initialization.BuildCompletionListener.completed()
        complete(result)
    }

    private fun complete(result: BuildResult) {
        val profile = buildProfile

        if (profile != null) {
            profile.setBuildFinished(clock.currentTime)

            try {
                listener.buildFinished(result, profile)
            } finally {
                buildProfile = null
                result.gradle?.removeListener(this)
            }
        }
    }

    override fun beforeEvaluate(project: Project) {
        val now = clock.currentTime
        buildProfile!!.getProjectProfile(project.path).configurationOperation.setStart(now)
    }

    override fun afterEvaluate(project: Project, state: ProjectState) {
        val now = clock.currentTime
        val projectProfile = buildProfile!!.getProjectProfile(project.path)
        projectProfile.configurationOperation.setFinish(now)
    }

    override fun beforeExecute(task: Task) {
        listener.beforeExecute(task)

        val now = clock.currentTime
        val project = task.project
        val projectProfile = buildProfile!!.getProjectProfile(project.path)
        val taskExecution = projectProfile.getTaskProfile(task.path)
        taskExecution.setStart(now)
    }

    override fun afterExecute(task: Task, state: TaskState) {
        val now = clock.currentTime
        val project = task.project
        val projectProfile = buildProfile!!.getProjectProfile(project.path)
        val taskExecution = projectProfile.getTaskProfile(task.path)
        taskExecution.setFinish(now)
        taskExecution.completed(state)

        listener.afterExecute(task, taskExecution)
    }
}
