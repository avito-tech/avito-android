package com.avito.android.baseline_profile

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.baseline_profile.BaselineProfileFiles.baselineProfileLocation
import com.avito.android.baseline_profile.BaselineProfileFiles.findProfileOrThrow
import com.avito.android.baseline_profile.BaselineProfileFiles.mainSrcDirectory
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.create
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import java.io.File

internal class ProfileTaskConfigurator(
    private val targetProject: Project,
    private val applicationProject: Provider<Project>,
    private val rootProject: Project,
    private val extension: ApplyBaselineProfileExtension,
) {
    private val loggerFactory = GradleLoggerPlugin.provideLoggerFactory(rootProject).get()
    private val logger = loggerFactory.create<ProfileTaskConfigurator>()
    private val gitOperations = GitClient(
        rootProjectDir = rootProject.projectDir,
        extension = extension.saveToVersionControl,
        loggerFactory = loggerFactory,
    )

    internal fun configure() {
        val assembleApplicationApkTask = configureAssembleApkTask()
        val instrumentationTask =
            targetProject.tasks.named(extension.instrumentationTaskName.get())
        val copyProfileTask = configureCopyToSourcesTask()
        val pushProfileTask = configureSaveToVersionControlTask()

        rootProject.tasks.register(extension.taskName.get()) { task ->
            task.description = "Apply baseline profile using instrumentation test run"

            if (gitOperations.isHeadCommitWithProfile()) {
                logger.warn("Skipping baseline profile generation - latest commit already updates profile")
                return@register
            }

            instrumentationTask.dependsOn(assembleApplicationApkTask.get())
            copyProfileTask.dependsOn(instrumentationTask)
            task.dependsOn(copyProfileTask)

            val shouldSaveToVersionControl = extension.saveToVersionControl.enable.getOrElse(false)
            if (shouldSaveToVersionControl) {
                task.finalizedBy(pushProfileTask)
            }
        }
    }

    private fun configureAssembleApkTask(): Provider<TaskProvider<Task>> {
        val taskName = "assemble" + extension.applicationVariantName.get().capitalized()
        return applicationProject.map { project -> project.tasks.named(taskName) }
    }

    private fun configureCopyToSourcesTask(): TaskProvider<Task> {
        val profileFromTestOutputs: Provider<File> = extension.macrobenchmarksOutputDirectory
            .map { outputs -> outputs.findProfileOrThrow() }
        val profileTargetLocation = applicationProject.map { prj -> prj.mainSrcDirectory() }

        return targetProject.tasks.register("copyBaselineProfile") { task ->
            task.description =
                "Copy baseline profile from ci artifacts directory to application source directory"

            task.doLast {
                rootProject.copy { copySpec ->
                    copySpec.apply {
                        duplicatesStrategy = DuplicatesStrategy.INCLUDE
                        from(profileFromTestOutputs)
                        into(profileTargetLocation)
                        rename { BaselineProfileFiles.baselineProfileFileName }
                    }
                }
            }
        }
    }

    private fun configureSaveToVersionControlTask(): TaskProvider<Task> {
        val profileLocation = applicationProject.map { prj -> prj.baselineProfileLocation() }

        return targetProject.tasks.register("saveProfileToVersionControl") { task ->
            task.description = "Push generated baseline profile into VCS"

            task.doLast {
                gitOperations.commitAndPushProfile(profileLocation.get().asFile.toPath())
            }
        }
    }
}
