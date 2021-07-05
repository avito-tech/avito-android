package com.avito.ci.steps

import com.avito.android.plugin.artifactory.artifactoryAppBackupTask
import com.avito.android.plugin.artifactory.artifactoryPassword
import com.avito.android.plugin.artifactory.artifactoryUser
import com.avito.cd.UploadCdBuildResultTask
import com.avito.cd.cdBuildConfig
import com.avito.cd.isCdBuildConfigPresent
import com.avito.cd.uploadCdBuildResultTaskName
import com.avito.kotlin.dsl.namedOrNull
import com.avito.upload_to_googleplay.deployTaskName
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

public class UploadBuildResult(context: String, name: String) : SuppressibleBuildStep(context, name) {

    @Suppress("MemberVisibilityCanBePrivate")
    public var uiTestConfiguration: String? = null

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (project.isCdBuildConfigPresent) {
            val uiTestConfiguration = uiTestConfiguration
            // TODO: can we validate a contract without cd build config?
            require(!uiTestConfiguration.isNullOrBlank()) {
                "uploadBuildResult.uiTestConfiguration parameter must be set"
            }

            val uploadCdBuildResult = project.tasks.register<UploadCdBuildResultTask>(
                name = uploadCdBuildResultTaskName
            ) {
                group = cdTaskGroup
                description = "Task for send CD build result"

                this.uiTestConfiguration.set(uiTestConfiguration)
                this.user.set(project.artifactoryUser)
                this.password.set(project.artifactoryPassword)
                this.suppressErrors.set(suppressFailures)

                project.tasks.namedOrNull(deployTaskName)?.also { deployTask -> dependsOn(deployTask) }

                mustRunAfter(project.tasks.artifactoryAppBackupTask())

                onlyIf {
                    !project.cdBuildConfig.map { it.outputDescriptor.skipUpload }.get()
                }
            }

            rootTask.configure { it.finalizedBy(uploadCdBuildResult) }
        }
    }
}
