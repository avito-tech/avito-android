package com.avito.android.gradle_configuration

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.CD_TASK_GROUP
import com.avito.android.DEFAULT_RELEASE_VARIANT
import com.avito.android.NupokatiExtension
import com.avito.android.agp.getVersionCode
import com.avito.android.artifactory_backup.ArtifactoryBackupTask
import com.avito.android.contract_upload.UploadCdBuildResultTask
import com.avito.android.model.input.CdBuildConfigParserFactory
import com.avito.android.model.input.CdBuildConfigV2
import com.avito.android.model.input.CdBuildConfigV3
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
    private val extensionV2: NupokatiExtension,
) {
    private val cdBuildConfigProvider = extensionV2.cdBuildConfigFile.map(CdBuildConfigParserFactory())
    private val variantName = extensionV2.releaseBuildVariantName.convention(DEFAULT_RELEASE_VARIANT)

    private val skipUploadSpec = Spec<Task> {
        if (!cdBuildConfigProvider.isPresent) {
            project.logger.lifecycle(
                "Skip uploading artifacts and contract json, " +
                    "because cdBuildConfigFile wasn't set"
            )
            return@Spec true
        }
        val skipUpload = cdBuildConfigProvider.get().outputDescriptor.skipUpload
        if (skipUpload) {
            project.logger.lifecycle(
                "Skip uploading artifacts and contract json, " +
                    "because skipUpload=true is called"
            )
        }
        val shouldRunTask = !skipUpload
        shouldRunTask
    }

    fun configure() {
        val nupokatiTask = project.tasks.register("nupokati") {
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
                        variantSlug, variant, skipUploadSpec
                    )
                val uploadCdBuildResultTask =
                    registerUploadCdBuildResult(
                        variantSlug, variant, publishArtifactsTask, skipUploadSpec
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
                    }
                }
            }
        }
    }

    private fun registerUploadCdBuildResult(
        variantSlug: String,
        variant: ApplicationVariant,
        publishArtifactsTask: TaskProvider<ArtifactoryBackupTask>,
        skipUploadSpec: Spec<Task>
    ) = project.tasks.register<UploadCdBuildResultTask>(uploadCdBuildResultTaskName(variantSlug)) {
        group = CD_TASK_GROUP
        description = "Send build result to Nupokati service"
        artifactoryUser.set(extensionV2.artifactory.login)
        artifactoryPassword.set(extensionV2.artifactory.password)
        reportViewerUrl.set(extensionV2.reportViewer.frontendUrl)
        reportCoordinates.set(extensionV2.reportViewer.reportCoordinates)
        teamcityBuildUrl.set(extensionV2.teamcityBuildUrl)
        cdBuildConfig.set(cdBuildConfigProvider)
        appVersionCode.set(variant.getVersionCode())
        buildOutputFileProperty.set(publishArtifactsTask.flatMap { it.buildOutput })

        dependsOn(publishArtifactsTask)
        onlyIf(skipUploadSpec)
    }

    private fun registerArtifactoryBackupTask(
        variantSlug: String,
        variant: ApplicationVariant,
        skipUploadSpec: Spec<Task>
    ) = project.tasks.register<ArtifactoryBackupTask>("artifactoryBackup$variantSlug") {
        group = CD_TASK_GROUP
        description = "Backup ${variant.name} artifacts in artifactory bucket"

        this.artifactoryUser.set(extensionV2.artifactory.login)
        this.artifactoryPassword.set(extensionV2.artifactory.password)
        this.artifactoryUploadPath.set(cdBuildConfigProvider.map {
            it.outputDescriptor.path.substringBeforeLast('/')
        })
        this.schemaVersion.set(cdBuildConfigProvider.map { it.schemaVersion })
        this.buildOutput.set(project.layout.buildDirectory.file("nupokati/buildOutput.json"))

        @Suppress("DEPRECATION")
        this.files.set(project.files(variant.artifacts.get(type = SingleArtifact.BUNDLE)))
        @Suppress("DEPRECATION")
        this.buildConfiguration.set(
            requireNotNull(variant.buildType) { "buildType should not be null here" }
        )
        onlyIf(skipUploadSpec)
    }
}
