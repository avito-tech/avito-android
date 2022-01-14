package com.avito.upload_to_googleplay

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

public const val deployTaskName: String = "deployToGooglePlay"

public fun TaskContainer.registerDeployToGooglePlayTask(
    deploys: List<GooglePlayDeploy>,
    configuration: Task.() -> Unit
): TaskProvider<out DefaultTask> {
    return register(deployTaskName, DeployToGooglePlayTask::class.java, deploys).apply {
        configure {
            it.description = "Upload binary to google play"
            it.group = "Google play"
        }
        configure(configuration)
    }
}

internal abstract class DeployToGooglePlayTask @Inject constructor(
    private val deploys: List<GooglePlayDeploy>
) : DefaultTask() {

    private val jsonKey = project.playConsoleJsonKey

    @TaskAction
    fun upload() {
        val googlePlayKey = jsonKey.orNull
            ?: throw IllegalStateException("google play key must present in ${project.name}")

        val deployer = GooglePlayDeployerFactoryProducer.create(googlePlayKey, mockWebServerUrl = null).create(logger)

        deployer.deploy(deploys)
    }
}
