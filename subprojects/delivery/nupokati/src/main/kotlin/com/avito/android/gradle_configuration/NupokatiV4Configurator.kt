package com.avito.android.gradle_configuration

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.CD_TASK_GROUP
import com.avito.android.DEFAULT_CHUNKED_UPLOAD_THRESHOLD
import com.avito.android.DEFAULT_NUPOKATI_CLIENT_CONNECTION_TIMEOUT
import com.avito.android.DEFAULT_NUPOKATI_CLIENT_READ_TIMEOUT
import com.avito.android.DEFAULT_NUPOKATI_CLIENT_WRITE_TIMEOUT
import com.avito.android.DEFAULT_RELEASE_VARIANT
import com.avito.android.agp.getVersionCode
import com.avito.android.artifacts_upload.UploadArtifactsTask
import com.avito.android.gradle_configuration.extension.NupokatiExtension
import com.avito.android.gradle_configuration.extension.ShouldUploadToNupokatiSpec
import com.avito.android.gradle_configuration.extension.spec.NupokatiV4PipelineSpec
import com.avito.android.http.nupokati.NupokatiV4ClientBuildService
import com.avito.android.model.input.config.parser.CdBuildConfigParserFactory
import com.avito.android.sendTestResultsTaskName
import com.avito.android.test_results_upload.SendTestResultsTask
import com.avito.android.uploadArtifactsTaskName
import com.avito.capitalize
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

internal class NupokatiV4Configurator(
    private val project: Project,
    private val pipelineSpec: NupokatiV4PipelineSpec,
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

        val nupokatiV4ClientBuildService = project.gradle.sharedServices.registerIfAbsent(
            "nupokatiV4Client",
            NupokatiV4ClientBuildService::class.java
        ) { spec ->
            spec.parameters.baseUrl.set(pipelineSpec.nupokatiUrl)
            spec.parameters.chunkedUploadThresholdBytes.set(
                pipelineSpec.chunkedUploadThresholdBytes.convention(
                    DEFAULT_CHUNKED_UPLOAD_THRESHOLD
                )
            )

            spec.parameters.connectionTimeoutSeconds.set(
                pipelineSpec.nupokatiClientConnectionTimeout.convention(
                    DEFAULT_NUPOKATI_CLIENT_CONNECTION_TIMEOUT
                )
            )
            spec.parameters.readTimeoutSeconds.set(
                pipelineSpec.nupokatiClientReadTimeout.convention(
                    DEFAULT_NUPOKATI_CLIENT_READ_TIMEOUT
                )
            )
            spec.parameters.writeTimeoutSeconds.set(
                pipelineSpec.nupokatiClientWriteTimeout.convention(
                    DEFAULT_NUPOKATI_CLIENT_WRITE_TIMEOUT
                )
            )
        }

        project.plugins.withType<AppPlugin> {
            val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
            val releaseVariantSelector = androidComponents.selector().withName(variantName.get())

            androidComponents.onVariants(selector = releaseVariantSelector) { variant: ApplicationVariant ->
                val uploadArtifactsTask =
                    registerUploadArtifactsTask(
                        pipelineSpec = pipelineSpec,
                        variant = variant,
                        nupokatiClientBuildService = nupokatiV4ClientBuildService,
                        shouldUploadToNupokatiSpec = shouldUploadToNupokatiSpec,
                    )
                val sendTestResultsTask =
                    registerSendTestResultsTask(
                        specName = specName,
                        variant = variant,
                        nupokatiClientBuildService = nupokatiV4ClientBuildService,
                        uploadArtifactsTask = uploadArtifactsTask,
                        shouldUploadToNupokatiSpec = shouldUploadToNupokatiSpec,
                    )
                nupokatiTask.dependsOn(sendTestResultsTask)
            }
        }
    }

    private fun registerSendTestResultsTask(
        specName: String,
        variant: ApplicationVariant,
        nupokatiClientBuildService: Provider<NupokatiV4ClientBuildService>,
        uploadArtifactsTask: TaskProvider<UploadArtifactsTask>,
        shouldUploadToNupokatiSpec: ShouldUploadToNupokatiSpec,
    ) = project.tasks.register<SendTestResultsTask>(sendTestResultsTaskName(specName)) {
        nupokatiClientService.set(nupokatiClientBuildService)
        cdBuildConfig.set(cdBuildConfigProvider)
        reportCoordinates.set(pipelineSpec.reportViewer.reportCoordinates)
        reportViewerUrl.set(pipelineSpec.reportViewer.frontendUrl)
        appVersionCode.set(variant.getVersionCode())

        dependsOn(uploadArtifactsTask)
        onlyIf(shouldUploadToNupokatiSpec)
    }

    private fun registerUploadArtifactsTask(
        pipelineSpec: NupokatiV4PipelineSpec,
        variant: ApplicationVariant,
        nupokatiClientBuildService: Provider<NupokatiV4ClientBuildService>,
        shouldUploadToNupokatiSpec: ShouldUploadToNupokatiSpec,
    ) = project.tasks.register<UploadArtifactsTask>(uploadArtifactsTaskName(pipelineSpec.name)) {
        nupokatiClientService.set(nupokatiClientBuildService)
        appVersionCode.set(variant.getVersionCode())
        cdBuildConfig.set(cdBuildConfigProvider)
        artifacts.set(pipelineSpec.artifacts)

        onlyIf(shouldUploadToNupokatiSpec)
    }
}
