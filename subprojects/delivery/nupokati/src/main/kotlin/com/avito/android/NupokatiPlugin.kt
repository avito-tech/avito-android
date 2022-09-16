package com.avito.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.agp.getVersionCode
import com.avito.android.artifactory_backup.ArtifactoryBackupTask
import com.avito.android.contract_upload.UploadCdBuildResultTask
import com.avito.android.model.input.CdBuildConfigParserFactory
import com.avito.android.model.input.CdBuildConfigV2
import com.avito.android.model.input.CdBuildConfigV3
import com.avito.android.stats.statsdConfig
import com.avito.capitalize
import com.avito.kotlin.dsl.withType
import com.avito.plugin.QAppsUploadTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

public class NupokatiPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        require(project.plugins.hasPlugin("com.avito.android.qapps")) {
            "Nupokati needs com.avito.android.qapps. Apply it before nupokati"
        }
        val extension = project.extensions.create<NupokatiExtension>("nupokati")

        val cdBuildConfig = extension.cdBuildConfigFile.map(CdBuildConfigParserFactory())

        val nupokatiTask = project.tasks.register("nupokati") {
            it.group = CD_TASK_GROUP
            it.description = "Root task for CD nupokati contract execution"
        }

        project.plugins.withType<AppPlugin> {
            val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()

            val releaseVariantSelector = androidComponents.selector()
                .withName(extension.releaseBuildVariantName.convention(DEFAULT_RELEASE_VARIANT).get())

            val skipUploadSpec = Spec<Task> {
                if (!cdBuildConfig.isPresent) {
                    project.logger.lifecycle(
                        "Skip uploading artifacts and contract json, " +
                            "because cdBuildConfigFile wasn't set"
                    )
                    return@Spec true
                }
                val skipUpload = cdBuildConfig.get().outputDescriptor.skipUpload
                if (skipUpload) {
                    project.logger.lifecycle(
                        "Skip uploading artifacts and contract json, " +
                            "because skipUpload=true is called"
                    )
                }
                val shouldRunTask = !skipUpload
                shouldRunTask
            }

            androidComponents.onVariants(selector = releaseVariantSelector) { variant: ApplicationVariant ->

                val variantSlug = variant.name.capitalize()

                val bundle: Provider<RegularFile> = variant.artifacts.get(type = SingleArtifact.BUNDLE)

                val publishArtifactsTask =
                    project.tasks.register<ArtifactoryBackupTask>("artifactoryBackup$variantSlug") {
                        group = CD_TASK_GROUP
                        description = "Backup ${variant.name} artifacts in artifactory bucket"

                        this.artifactoryUser.set(extension.artifactory.login)
                        this.artifactoryPassword.set(extension.artifactory.password)
                        this.artifactoryUploadPath.set(cdBuildConfig.map {
                            it.outputDescriptor.path.substringBeforeLast('/')
                        })
                        this.schemaVersion.set(cdBuildConfig.map { it.schemaVersion })
                        this.statsDConfig.set(project.statsdConfig)
                        this.buildOutput.set(project.layout.buildDirectory.file("nupokati/buildOutput.json"))

                        @Suppress("DEPRECATION")
                        this.files.set(project.files(bundle))
                        @Suppress("DEPRECATION")
                        this.buildConfiguration.set(
                            requireNotNull(variant.buildType) { "buildType should not be null here" }
                        )
                        onlyIf(skipUploadSpec)
                    }

                val uploadCdBuildResultTask =
                    project.tasks.register<UploadCdBuildResultTask>(uploadCdBuildResultTaskName(variantSlug)) {
                        group = CD_TASK_GROUP
                        description = "Send build result to Nupokati service"

                        this.artifactoryUser.set(extension.artifactory.login)
                        this.artifactoryPassword.set(extension.artifactory.password)
                        this.reportViewerUrl.set(extension.reportViewer.frontendUrl)
                        this.reportCoordinates.set(extension.reportViewer.reportCoordinates)
                        this.teamcityBuildUrl.set(extension.teamcityBuildUrl)
                        this.cdBuildConfig.set(cdBuildConfig)
                        this.appVersionCode.set(variant.getVersionCode())
                        this.buildOutputFileProperty.set(publishArtifactsTask.flatMap { it.buildOutput })
                        this.statsDConfig.set(project.statsdConfig)

                        dependsOn(publishArtifactsTask)

                        onlyIf(skipUploadSpec)
                    }

                nupokatiTask.dependsOn(uploadCdBuildResultTask)

                project.afterEvaluate {
                    if (cdBuildConfig.isPresent) {
                        when (val config = cdBuildConfig.get()) {
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
    }
}
