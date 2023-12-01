package com.avito.android.baseline_profile.internal

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.baseline_profile.ApplyProfileToSourceCodeTask
import com.avito.android.baseline_profile.configuration.ApplyBaselineProfileConfiguration
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.create
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register

internal class ProfileTaskConfigurator(
    private val taskName: String,
    private val targetProject: Project,
    private val applicationProject: Provider<Project>,
    private val rootProject: Project,
    private val configuration: ApplyBaselineProfileConfiguration,
) {
    private val loggerFactory = GradleLoggerPlugin.provideLoggerFactory(rootProject).get()
    private val logger = loggerFactory.create<ProfileTaskConfigurator>()
    private val gitOperations = GitClient(
        rootProjectDir = rootProject.projectDir,
        extension = configuration.saveToVersionControl,
        loggerFactory = loggerFactory,
    )

    internal fun configure() {
        val assembleApplicationApkTask = configureAssembleApkTask()
        val instrumentationTask: Provider<TaskProvider<Task>> =
            configuration.instrumentationTaskName.map { targetProject.tasks.named(it) }
        val applyToSourceCodeTask = configureApplyProfileToSourceCodeTask()

        // rootProject here is required to configure assembleApplicationTask, which is not part of [targetProject],
        // but rather part of [applicationProject]
        rootProject.tasks.register(taskName) { task ->
            task.description = "Apply baseline profile using instrumentation test run"

            configuration.validateValues()

            if (gitOperations.isHeadCommitWithProfile()) {
                logger.warn("Skipping baseline profile generation - latest commit already updates profile")
                return@register
            }

            instrumentationTask.get().dependsOn(assembleApplicationApkTask.get())
            applyToSourceCodeTask.dependsOn(instrumentationTask.get())
            task.dependsOn(applyToSourceCodeTask)
        }
    }

    private fun configureAssembleApkTask(): Provider<TaskProvider<Task>> {
        return applicationProject.map { project ->
            val taskName = "assemble" + configuration.applicationVariantName.get().capitalized()
            project.tasks.named(taskName)
        }
    }

    private fun configureApplyProfileToSourceCodeTask(): TaskProvider<ApplyProfileToSourceCodeTask> =
        targetProject.tasks.register<ApplyProfileToSourceCodeTask>(ApplyProfileToSourceCodeTask.taskName) {
            applicationModuleName.set(configuration.applicationModuleName)
            testOutputsDirectory.set(configuration.macrobenchmarksOutputDirectory)
            extension.set(configuration.saveToVersionControl)
        }
}
