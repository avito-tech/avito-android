package com.avito.android.gradle_configuration

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.CD_TASK_GROUP
import com.avito.android.DEFAULT_RELEASE_VARIANT
import com.avito.android.agp.getVersionCode
import com.avito.android.artifactory_backup.ArtifactoryBackupTask
import com.avito.android.contract_upload.UploadCdBuildResultTask
import com.avito.android.gradle_configuration.extension.ShouldUploadToNupokatiSpec
import com.avito.android.gradle_configuration.extension.spec.NupokatiV2PipelineSpec
import com.avito.android.model.input.config.CdBuildConfigV2
import com.avito.android.uploadCdBuildResultTaskName
import com.avito.capitalize
import com.avito.kotlin.dsl.withType
import com.avito.plugin.QAppsUploadTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

internal class NupokatiV2Configurator(
    private val project: Project,
    private val pipelineSpec: NupokatiV2PipelineSpec,
) {
    private val config = pipelineSpec.cdBuildConfig

    private val shouldUploadToNupokatiSpec = ShouldUploadToNupokatiSpec(
        project = project,
        cdBuildConfigProvider = config.map { it }
    )

    fun configure() {
        val specName = pipelineSpec.name.capitalize()
        val nupokatiTask = project.tasks.register("nupokati$specName") {
            it.group = CD_TASK_GROUP
            it.description = "Root task for CD nupokati contract execution"
        }

        project.plugins.withType<AppPlugin> {
            val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
            val releaseVariantSelector = androidComponents.selector()
                .withName(DEFAULT_RELEASE_VARIANT)

            androidComponents.onVariants(selector = releaseVariantSelector) { variant: ApplicationVariant ->
                val variantSlug = variant.name.capitalize()
                val publishArtifactsTask =
                    registerArtifactoryBackupTask(
                        variantSlug, variant, shouldUploadToNupokatiSpec
                    )
                val uploadCdBuildResultTask =
                    registerUploadCdBuildResult(
                        variantSlug, variant, publishArtifactsTask, shouldUploadToNupokatiSpec
                    )
                nupokatiTask.dependsOn(uploadCdBuildResultTask)
            }

            project.afterEvaluate {
                if (config.isPresent) {
                    val qapps = config.get().deployments.filterIsInstance<CdBuildConfigV2.Deployment.Qapps>()
                    val isRelease = qapps.any { it.isRelease }
                    project.tasks.withType<QAppsUploadTask>().configureEach {
                        it.releaseChain.set(isRelease)
                        it.onlyIf {
                            qapps.isNotEmpty()
                        }
                    }
                }
            }
        }
    }

    private fun registerUploadCdBuildResult(
        variantSlug: String,
        variant: ApplicationVariant,
        publishArtifactsTask: TaskProvider<ArtifactoryBackupTask>,
        shouldRunSpec: Spec<Task>,
    ) = project.tasks.register<UploadCdBuildResultTask>(uploadCdBuildResultTaskName(variantSlug)) {
        group = CD_TASK_GROUP
        description = "Send build result to Nupokati service"
        artifactoryUser.set(pipelineSpec.artifactory.login)
        artifactoryPassword.set(pipelineSpec.artifactory.password)
        reportViewerUrl.set(pipelineSpec.reportViewer.frontendUrl)
        reportCoordinates.set(pipelineSpec.reportViewer.reportCoordinates)
        teamcityBuildUrl.set(pipelineSpec.teamcityBuildUrl)
        cdBuildConfig.set(config)
        appVersionCode.set(variant.getVersionCode())
        buildOutputFileProperty.set(publishArtifactsTask.flatMap { it.buildOutput })

        dependsOn(publishArtifactsTask)
        onlyIf(shouldRunSpec)
    }

    private fun registerArtifactoryBackupTask(
        variantSlug: String,
        variant: ApplicationVariant,
        shouldRunSpec: Spec<Task>,
    ) = project.tasks.register<ArtifactoryBackupTask>("artifactoryBackup$variantSlug") {
        group = CD_TASK_GROUP
        description = "Backup ${variant.name} artifacts in artifactory bucket"

        this.artifactoryUser.set(pipelineSpec.artifactory.login)
        this.artifactoryPassword.set(pipelineSpec.artifactory.password)
        this.buildOutput.set(project.layout.buildDirectory.file("nupokati/buildOutput.json"))
        this.artifactoryUploadPath.set(config.map { it.outputDescriptor.path.substringBeforeLast('/') })
        this.schemaVersion.set(config.map { it.schemaVersion })

        @Suppress("DEPRECATION")
        this.files.set(project.files(variant.artifacts.get(type = SingleArtifact.BUNDLE)))
        @Suppress("DEPRECATION")
        this.buildConfiguration.set(
            requireNotNull(variant.buildType) { "buildType should not be null here" }
        )
        onlyIf(shouldRunSpec)
    }
}
