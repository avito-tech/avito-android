package com.avito.upload_to_googleplay

import com.avito.logger.GradleLoggerFactory
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

public const val deployTaskName: String = "deployToGooglePlay"

public fun TaskContainer.registerDeployToGooglePlayTask(
    project: Project,
    deploys: List<GooglePlayDeploy>,
    configuration: Task.() -> Unit
): TaskProvider<out DefaultTask> {
    return register(deployTaskName, DeployToGooglePlayTask::class.java, deploys).apply {
        configure {
            it.description = "Upload binary to google play"
            it.group = "Google play"

            it.loggerFactory.set(
                GradleLoggerFactory.fromProject(
                    project = project,
                    pluginName = "CiStepsPlugin",
                    taskName = "DeployToGooglePlayTask"
                )
            )
        }
        configure(configuration)
    }
}

internal abstract class DeployToGooglePlayTask @Inject constructor(
    private val deploys: List<GooglePlayDeploy>
) : DefaultTask() {

    private val jsonKey = project.playConsoleJsonKey

    @get:Internal
    abstract val loggerFactory: Property<LoggerFactory>

    @TaskAction
    fun upload() {
        val logger = loggerFactory.get().create<DeployToGooglePlayTask>()

        val googlePlayKey = jsonKey.orNull
            ?: throw IllegalStateException("google play key must present in ${project.name}").apply {
                logger.critical("google play key was empty", this)
            }
        val deployer = GooglePlayDeployerImpl(googlePlayKey, logger)
        deployer.deploy(deploys)
    }
}
