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
import com.avito.android.gradle_configuration.extension.NupokatiExtension
import com.avito.android.gradle_configuration.extension.ShouldUploadToNupokatiSpec
import com.avito.android.gradle_configuration.extension.spec.NupokatiV2PipelineSpec
import com.avito.android.model.input.config.CdBuildConfigV2
import com.avito.android.model.input.config.CdBuildConfigV3
import com.avito.android.model.input.config.CdBuildConfigV4
import com.avito.android.model.input.config.parser.CdBuildConfigParserFactory
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
    extension: NupokatiExtension,
) {
    private val cdBuildConfigProvider = extension.cdBuildConfigFile.map(CdBuildConfigParserFactory())
    private val variantName = pipelineSpec.releaseBuildVariantName.convention(DEFAULT_RELEASE_VARIANT)

    private val shouldUploadToNupokatiSpec = ShouldUploadToNupokatiSpec(
        project = project,
        cdBuildConfigProvider = cdBuildConfigProvider
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
                .withName(variantName.get())

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
                if (cdBuildConfigProvider.isPresent) {
                    when (val config = cdBuildConfigProvider.get()) {
                        is CdBuildConfigV2 -> {
                            val qapps = config.deployments.filterIsInstance<CdBuildConfigV2.Deployment.Qapps>()
                            val isRelease = qapps.any { it.isRelease }
                            project.tasks.withType<QAppsUploadTask>().configureEach {
                                it.releaseChain.set(isRelease)
                                it.onlyIf {
                                    qapps.isNotEmpty()
                                }
                            }
                        }

                        is CdBuildConfigV3 -> throw UnsupportedOperationException(
                            "Fail to evaluate project. CdBuildConfigV3 currently unsupported"
                        )

                        is CdBuildConfigV4 -> throw UnsupportedOperationException(
                            "Fail to evaluate project. For schema version == 4, use v4 extension instead"
                        )
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
        cdBuildConfig.set(cdBuildConfigProvider)
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
        this.artifactoryUploadPath.set(cdBuildConfigProvider.map {
            when (it) {
                is CdBuildConfigV2 -> it.outputDescriptor.path.substringBeforeLast('/')
                else ->
                    throw IllegalArgumentException("Unsupported cd config version: ${it.schemaVersion}")
            }
        })
        this.schemaVersion.set(cdBuildConfigProvider.map { it.schemaVersion })
        this.buildOutput.set(project.layout.buildDirectory.file("nupokati/buildOutput.json"))

        @Suppress("DEPRECATION")
        this.files.set(project.files(variant.artifacts.get(type = SingleArtifact.BUNDLE)))
        @Suppress("DEPRECATION")
        this.buildConfiguration.set(
            requireNotNull(variant.buildType) { "buildType should not be null here" }
        )
        onlyIf(shouldRunSpec)
    }
}
