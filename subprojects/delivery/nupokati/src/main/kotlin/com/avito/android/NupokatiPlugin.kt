package com.avito.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.AppPlugin
import com.avito.android.agp.getVersionCode
import com.avito.android.artifactory_backup.ArtifactoryPublishTask
import com.avito.android.contract_upload.UploadCdBuildResultTask
import com.avito.android.google_play.DeployBundleToGooglePlayTask
import com.avito.android.model.BuildOutput
import com.avito.android.model.CdBuildConfig
import com.avito.android.provider.CdBuildConfigTransformer
import com.avito.android.provider.CdBuildConfigValidator
import com.avito.android.stats.statsdConfig
import com.avito.capitalize
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

public class NupokatiPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<NupokatiExtension>("nupokati")

        val cdBuildConfig: Provider<CdBuildConfig> = project.providers
            .gradleProperty("cd.build.config.file")
            .forUseAtConfigurationTime()
            .map(
                CdBuildConfigTransformer(
                    rootProjectLayout = project.rootProject.layout,
                    validator = CdBuildConfigValidator()
                )
            )

        val buildOutput = BuildOutput()

        project.plugins.withType<AppPlugin> {
            val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()

            val releaseVariantSelector = androidComponents.selector()
                .withName(extension.releaseBuildVariantName.convention(DEFAULT_RELEASE_VARIANT).get())

            androidComponents.onVariants(selector = releaseVariantSelector) { variant: ApplicationVariant ->

                val variantSlug = variant.name.capitalize()

                val bundle = variant.artifacts.get(type = SingleArtifact.BUNDLE)

                // will be removed in MBS-12535
                val uploadToGooglePlayTask =
                    project.tasks.register<DeployBundleToGooglePlayTask>("deployToGooglePlay$variantSlug") {
                        group = CD_TASK_GROUP
                        description = "Deploy bundle to GooglePlay"

                        this.bundle.set(bundle)
                        this.mapping.set(variant.artifacts.get(type = SingleArtifact.OBFUSCATION_MAPPING_FILE))
                        this.applicationId.set(variant.applicationId)
                        this.track.set(extension.googlePlayTrack)

                        /**
                         * when I try to find task provider via project.tasks.named I get Exception that there is no task
                         * That's because firebase-crashlytics-plugin creates task some how after we trying bind to it
                         */
                        dependsOn("${project.path}:uploadCrashlyticsMappingFile$variantSlug")
                    }

                val publishArtifactsTask =
                    project.tasks.register<ArtifactoryPublishTask>("artifactoryPublish$variantSlug") {
                        group = CD_TASK_GROUP
                        description = "Publish release artifacts"

                        this.artifactoryUrl.set(extension.artifactory.baseUrl)
                        this.artifactoryUser.set(extension.artifactory.login)
                        this.artifactoryPassword.set(extension.artifactory.password)
                        this.repository.set(
                            extension.artifactory.backupRepository.convention("apps-release-local")
                        )
                        this.projectName.set(
                            extension.artifactory.projectName.convention("${project.name}-android")
                        )
                        this.projectType.set(extension.artifactory.projectType)
                        this.version.set(extension.artifactory.version)
                        this.files.set(project.files(bundle))
                    }

                project.tasks.register<UploadCdBuildResultTask>(uploadCdBuildResultTaskName(variantSlug)) {
                    group = CD_TASK_GROUP
                    description = "Send build result to Nupokati service"

                    this.artifactoryUser.set(extension.artifactory.login)
                    this.artifactoryPassword.set(extension.artifactory.password)
                    this.suppressErrors.set(extension.suppressFailures)
                    this.reportViewerUrl.set(extension.reportViewerUrl)
                    this.reportCoordinates.set(extension.reportCoordinates)
                    this.teamcityBuildUrl.set(extension.teamcityBuildUrl)
                    this.cdBuildConfig.set(cdBuildConfig)
                    this.appVersionCode.set(variant.getVersionCode())
                    this.buildOutput.set(buildOutput)
                    this.statsDConfig.set(project.statsdConfig)

                    // todo depend on output with actually uploaded artifacts
                    dependsOn(publishArtifactsTask)

                    dependsOn(uploadToGooglePlayTask)

                    // todo firebase crashlytics task
                }
            }
        }
    }
}
