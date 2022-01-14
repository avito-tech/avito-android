package com.avito.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.AppPlugin
import com.avito.android.agp.getVersionCode
import com.avito.android.artifactory_backup.ArtifactoryPublishTask
import com.avito.android.contract_upload.UploadCdBuildResultTask
import com.avito.android.google_play.GooglePlayUploadTaskConfigurator
import com.avito.android.model.BuildOutput
import com.avito.android.model.CdBuildConfig
import com.avito.android.provider.CdBuildConfigTransformer
import com.avito.android.provider.CdBuildConfigValidator
import com.avito.android.stats.statsdConfig
import com.avito.capitalize
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

public class NupokatiPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<NupokatiExtension>("nupokati")

        val googlePlayUploadTaskConfigurator = GooglePlayUploadTaskConfigurator(project, extension)

        val buildOutput = BuildOutput()

        project.plugins.withType<AppPlugin> {
            val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()

            val releaseVariantSelector = androidComponents.selector()
                .withName(extension.releaseBuildVariantName.convention(DEFAULT_RELEASE_VARIANT).get())

            androidComponents.onVariants(selector = releaseVariantSelector) { variant: ApplicationVariant ->

                val variantSlug = variant.name.capitalize()

                val bundle: Provider<RegularFile> = variant.artifacts.get(type = SingleArtifact.BUNDLE)

                val cdBuildConfig: Provider<CdBuildConfig> = extension.cdBuildConfigFile
                    .map(CdBuildConfigTransformer(validator = CdBuildConfigValidator()))

                val uploadToGooglePlayTask = googlePlayUploadTaskConfigurator.configure(
                    cdBuildConfig = cdBuildConfig,
                    variant = variant,
                    bundle = bundle
                )

                val publishArtifactsTask =
                    project.tasks.register<ArtifactoryPublishTask>("artifactoryPublish$variantSlug") {
                        group = CD_TASK_GROUP
                        description = "Publish release artifacts"

                        this.artifactoryUser.set(extension.artifactory.login)
                        this.artifactoryPassword.set(extension.artifactory.password)
                        this.artifactoryUploadPath.set(
                            cdBuildConfig.map {
                                it.outputDescriptor.path.substringBeforeLast('/')
                            }
                        )

                        // todo need to check sign somehow, because it's completely possible to miss it here
                        this.files.set(project.files(bundle))
                        this.statsDConfig.set(project.statsdConfig)
                    }

                project.tasks.register<UploadCdBuildResultTask>(uploadCdBuildResultTaskName(variantSlug)) {
                    group = CD_TASK_GROUP
                    description = "Send build result to Nupokati service"

                    this.artifactoryUser.set(extension.artifactory.login)
                    this.artifactoryPassword.set(extension.artifactory.password)
                    this.suppressErrors.set(extension.suppressFailures)
                    this.reportViewerUrl.set(extension.reportViewer.frontendUrl)
                    this.reportCoordinates.set(extension.reportViewer.reportCoordinates)
                    this.teamcityBuildUrl.set(extension.teamcityBuildUrl)
                    this.cdBuildConfig.set(cdBuildConfig)
                    this.appVersionCode.set(variant.getVersionCode())
                    this.buildOutput.set(buildOutput)
                    this.statsDConfig.set(project.statsdConfig)

                    // todo depend on output with actually uploaded artifacts
                    dependsOn(publishArtifactsTask)

                    if (uploadToGooglePlayTask != null) {
                        dependsOn(uploadToGooglePlayTask)
                    }
                }
            }
        }
    }
}
