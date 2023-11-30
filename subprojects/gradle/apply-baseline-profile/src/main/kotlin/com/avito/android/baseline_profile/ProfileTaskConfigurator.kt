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
        val copyProfileTask = configureCopyToSourcesTask()
        val pushProfileTask = configureSaveToVersionControlTask()

        rootProject.tasks.register(taskName) { task ->
            configuration.validateValues()

            task.description = "Apply baseline profile using instrumentation test run"

            if (gitOperations.isHeadCommitWithProfile()) {
                logger.warn("Skipping baseline profile generation - latest commit already updates profile")
                return@register
            }

            instrumentationTask.get().dependsOn(assembleApplicationApkTask.get())
            copyProfileTask.dependsOn(instrumentationTask.get())
            task.dependsOn(copyProfileTask)

            val shouldSaveToVersionControl = configuration.saveToVersionControl.enable.getOrElse(false)
            if (shouldSaveToVersionControl) {
                task.finalizedBy(pushProfileTask)
            }
        }
    }

    private fun configureAssembleApkTask(): Provider<TaskProvider<Task>> {
        return applicationProject.map { project ->
            val taskName = "assemble" + configuration.applicationVariantName.get().capitalized()
            project.tasks.named(taskName)
        }
    }

    private fun configureCopyToSourcesTask(): TaskProvider<Task> {
        val profileFromTestOutputs: Provider<File> = configuration.macrobenchmarksOutputDirectory
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
