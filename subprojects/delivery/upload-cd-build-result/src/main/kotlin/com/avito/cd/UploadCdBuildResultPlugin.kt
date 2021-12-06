package com.avito.cd

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class UploadCdBuildResultPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<UploadCdBuildResultExtension>("uploadCdBuildResult")

        project.tasks.register<UploadCdBuildResultTask>(uploadCdBuildResultTaskName) {
            group = CD_TASK_GROUP
            description = "Task for sending CD build result to service"

            this.artifactoryUser.set(extension.artifactoryUser)
            this.artifactoryPassword.set(extension.artifactoryPassword)
            this.suppressErrors.set(extension.suppressFailures)
        }
    }
}
