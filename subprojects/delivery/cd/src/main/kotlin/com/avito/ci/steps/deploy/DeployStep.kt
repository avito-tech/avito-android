package com.avito.ci.steps.deploy

import com.avito.android.plugin.artifactory.artifactoryAppBackupTask
import com.avito.cd.CdBuildConfig
import com.avito.cd.cdBuildConfig
import com.avito.cd.isCdBuildConfigPresent
import com.avito.ci.steps.SuppressibleBuildStep
import com.avito.ci.steps.verifyTaskName
import com.avito.upload_to_googleplay.GooglePlayDeploy
import com.avito.upload_to_googleplay.registerDeployToGooglePlayTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

public class DeployStep internal constructor(
    context: String,
    private val transformer: ToGooglePlayDeploysTransformer,
    private val provider: UploadCrashlyticsProguardFileTasksProvider,
    name: String
) : SuppressibleBuildStep(context, name) {

    /**
     * Use to enable or disable sending proguard mapping to Firebase-crashlytics
     *
     *
     * release -> signViaService -> packageRelease -> ...
     *
     * firebase-crashlytics-plugin
     * uploadCrashlyticsProguardMappingReleaseFile -> :avito:assembleRelease
     *
     * This hack add direct dependency
     * deployToGooglePlay -> uploadCrashlyticsProguardMappingReleaseFile
     */
    public var uploadCrashlyticsProguardMappingFile: Boolean = false

    override fun registerTask(
        project: Project,
        rootTask: TaskProvider<out Task>
    ) {
        if (project.isCdBuildConfigPresent) {
            val deployments = project.cdBuildConfig.get().deployments
            registerDeploymentsTasks(
                deployments.filterIsInstance<CdBuildConfig.Deployment.GooglePlay>(),
                project,
                rootTask
            )
        } else {
            project.logger.lifecycle("Configure deploy step without cd build config")
        }
    }

    private fun registerDeploymentsTasks(
        deployments: List<CdBuildConfig.Deployment.GooglePlay>,
        project: Project,
        rootTask: TaskProvider<out Task>
    ) {
        val googlePlayDeploys = mapDeploymentsToGooglePlayDeploys(deployments)
        if (googlePlayDeploys.isNotEmpty()) {
            val uploadToPlayMarketTask = project.tasks.registerDeployToGooglePlayTask(
                deploys = googlePlayDeploys
            ) {
                dependsOn(verifyTaskName(context))
                dependsOn(project.tasks.artifactoryAppBackupTask())
                if (uploadCrashlyticsProguardMappingFile) {
                    dependsOn(*provider.provide(project, deployments).toTypedArray())
                }
            }
            rootTask.configure { it.finalizedBy(uploadToPlayMarketTask) }
        }
    }

    private fun mapDeploymentsToGooglePlayDeploys(
        deployments: List<CdBuildConfig.Deployment.GooglePlay>
    ): List<GooglePlayDeploy> = deployments.map(transformer::transform)
}
