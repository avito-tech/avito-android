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
    private val mappingFileTaskProvider: CrashlyticsTaskProvider,
    private val nativeSymbolsTaskProvider: CrashlyticsTaskProvider,
    name: String
) : SuppressibleBuildStep(context, name) {

    /**
     * Upload proguard mapping to Firebase-crashlytics
     * See CrashlyticsExtension.mappingFileUploadEnabled
     *
     * release -> signViaService -> packageRelease -> ...
     *
     * firebase-crashlytics-plugin
     * uploadCrashlyticsProguardMappingReleaseFile -> :avito:assembleRelease
     *
     * This hack adds direct dependency
     * deployToGooglePlay -> uploadCrashlyticsProguardMapping<build variant>File
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public var uploadCrashlyticsProguardMappingFile: Boolean = false

    /**
     * Upload proguard mapping to Firebase-crashlytics
     * See CrashlyticsExtension.nativeSymbolUploadEnabled
     * https://firebase.google.com/docs/crashlytics/ndk-reports#alternative-options-symbol-uploading
     *
     * This hack adds direct dependency
     * deployToGooglePlay -> uploadCrashlyticsSymbolFile<build variant>
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public var uploadCrashlyticsNativeSymbols: Boolean = false

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
            project.logger.lifecycle("Skip deploy step due to no cd build config")
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
                    dependsOn(*mappingFileTaskProvider.provide(project, deployments).toTypedArray())
                }
                if (uploadCrashlyticsNativeSymbols) {
                    dependsOn(*nativeSymbolsTaskProvider.provide(project, deployments).toTypedArray())
                }
            }
            rootTask.configure { it.finalizedBy(uploadToPlayMarketTask) }
        }
    }

    private fun mapDeploymentsToGooglePlayDeploys(
        deployments: List<CdBuildConfig.Deployment.GooglePlay>
    ): List<GooglePlayDeploy> = deployments.map(transformer::transform)
}
