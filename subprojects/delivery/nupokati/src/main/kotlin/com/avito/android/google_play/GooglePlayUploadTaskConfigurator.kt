package com.avito.android.google_play

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import com.avito.android.CD_TASK_GROUP
import com.avito.android.NupokatiExtension
import com.avito.android.model.AndroidArtifactType
import com.avito.android.model.CdBuildConfig
import com.avito.capitalize
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

/**
 * will be removed in MBS-12535
 */
internal class GooglePlayUploadTaskConfigurator(
    private val project: Project,
    private val extension: NupokatiExtension
) {

    internal fun configure(
        cdBuildConfig: Provider<CdBuildConfig>,
        variant: ApplicationVariant,
        bundle: Provider<RegularFile>,
    ): TaskProvider<DeployBundleToGooglePlayTask>? {

        val variantSlug = variant.name.capitalize()

        val googlePlayDeploymentProvider: Provider<CdBuildConfig.Deployment.GooglePlay> = cdBuildConfig.flatMap {
            project.providers.provider { it.findGooglePlayDeployment() }
        }

        if (!googlePlayDeploymentProvider.isPresent) {
            project.logger.lifecycle(
                "Google play upload skipped. " +
                    "Reason: google play deployment was not requested in config"
            )
            return null
        }

        val googlePlayDeployment = googlePlayDeploymentProvider.get()

        if (!googlePlayDeployment.buildVariant.equals(variant.name, ignoreCase = true)) {
            project.logger.lifecycle(
                "Google play upload skipped. " +
                    "Reason: supported variant: ${variant.name}; requested: ${googlePlayDeployment.buildVariant}"
            )
            return null
        }

        if (googlePlayDeployment.artifactType != AndroidArtifactType.BUNDLE) {
            project.logger.lifecycle(
                "Google play upload skipped. " +
                    "Reason: only bundle binary supported by google play store; " +
                    "requested: ${googlePlayDeployment.artifactType}"
            )
            return null
        }

        project.logger.lifecycle("Google play upload initianted: $googlePlayDeployment")

        return project.tasks.register<DeployBundleToGooglePlayTask>("deployToGooglePlay$variantSlug") {
            group = CD_TASK_GROUP
            description = "Deploy bundle to GooglePlay"

            this.googlePlayKeyFile.set(extension.googlePlay.keyFile)
            this.mockGooglePlayUrl.set(extension.googlePlay.mockUrl)
            this.bundle.set(bundle)
            this.mapping.set(variant.artifacts.get(type = SingleArtifact.OBFUSCATION_MAPPING_FILE))
            this.applicationId.set(variant.applicationId)
            this.track.set(googlePlayDeployment.track)

            if (extension.uploadCrashlyticsMapping.convention(true).get()) {
                /**
                 * when I try to find task provider via project.tasks.named I get Exception that there is no task
                 * That's because firebase-crashlytics-plugin creates task some how after we trying bind to it
                 */
                dependsOn("${project.path}:uploadCrashlyticsMappingFile$variantSlug")
            }
        }
    }

    private fun CdBuildConfig.findGooglePlayDeployment(): CdBuildConfig.Deployment.GooglePlay? {
        return deployments
            .singleOrNull { it is CdBuildConfig.Deployment.GooglePlay } as? CdBuildConfig.Deployment.GooglePlay
    }
}
