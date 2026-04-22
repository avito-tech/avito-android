package com.avito.android.gradle_configuration

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.CD_TASK_GROUP
import com.avito.android.DEFAULT_CHUNKED_UPLOAD_THRESHOLD
import com.avito.android.DEFAULT_NUPOKATI_CLIENT_CONNECTION_TIMEOUT
import com.avito.android.DEFAULT_NUPOKATI_CLIENT_READ_TIMEOUT
import com.avito.android.DEFAULT_NUPOKATI_CLIENT_WRITE_TIMEOUT
import com.avito.android.artifacts_upload.UploadArtifactsTask
import com.avito.android.gradle_configuration.extension.ArtifactV4
import com.avito.android.gradle_configuration.extension.ShouldUploadToNupokatiSpec
import com.avito.android.gradle_configuration.extension.spec.NupokatiV4PipelineSpec
import com.avito.android.http.nupokati.NupokatiV4ClientTask
import com.avito.android.sendTestResultsTaskName
import com.avito.android.stats.statsdConfig
import com.avito.android.test_results_upload.SendTestResultsTask
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.android.uploadArtifactsTaskName
import com.avito.capitalize
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

internal class NupokatiV4Configurator(
    private val project: Project,
    private val pipelineSpec: NupokatiV4PipelineSpec,
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

        val uploadArtifactsTask =
            registerUploadArtifactsTask(
                pipelineSpec = pipelineSpec,
                shouldUploadToNupokatiSpec = shouldUploadToNupokatiSpec,
            )
        val sendTestResultsTask =
            registerSendTestResultsTask(
                specName = specName,
                pipelineSpec = pipelineSpec,
                uploadArtifactsTask = uploadArtifactsTask,
                shouldUploadToNupokatiSpec = shouldUploadToNupokatiSpec,
            )
        nupokatiTask.dependsOn(sendTestResultsTask)
    }

    private fun registerSendTestResultsTask(
        specName: String,
        pipelineSpec: NupokatiV4PipelineSpec,
        uploadArtifactsTask: TaskProvider<UploadArtifactsTask>,
        shouldUploadToNupokatiSpec: ShouldUploadToNupokatiSpec,
    ) = project.tasks.register<SendTestResultsTask>(sendTestResultsTaskName(specName)) {
        configureClientProperties(this, pipelineSpec)
        cdBuildConfig.set(config)
        reportCoordinates.set(pipelineSpec.reportViewer.reportCoordinates)
        reportViewerUrl.set(pipelineSpec.reportViewer.frontendUrl)
        appVersionCode.set(pipelineSpec.versionCode)

        dependsOn(uploadArtifactsTask)
        onlyIf(shouldUploadToNupokatiSpec)
    }

    private fun registerUploadArtifactsTask(
        pipelineSpec: NupokatiV4PipelineSpec,
        shouldUploadToNupokatiSpec: ShouldUploadToNupokatiSpec,
    ) = project.tasks.register<UploadArtifactsTask>(uploadArtifactsTaskName(pipelineSpec.name)) {
        configureClientProperties(this, pipelineSpec)
        appVersionCode.set(pipelineSpec.versionCode)
        cdBuildConfig.set(config)

        val storeNamesProvider = pipelineSpec.artifacts.map { list ->
            list.map { artifact ->
                val storeName = when (artifact) {
                    is ArtifactV4.AppBinary -> artifact.storeName
                    is ArtifactV4.Artifact -> null
                }
                val fileName = artifact.file.get().name
                fileName to storeName
            }.associate { it }.filter { it.value != null }
        }
        storeNames.set(storeNamesProvider)

        val artifactsProvider = pipelineSpec.artifacts.map { list ->
            list.map { it.file }
        }
        artifacts.from(artifactsProvider)

        onlyIf(shouldUploadToNupokatiSpec)
    }

    private fun configureClientProperties(task: NupokatiV4ClientTask, pipelineSpec: NupokatiV4PipelineSpec) {
        task.baseUrl.set(pipelineSpec.nupokatiUrl)
        task.chunkedUploadThresholdBytes.set(
            pipelineSpec.chunkedUploadThresholdBytes.convention(DEFAULT_CHUNKED_UPLOAD_THRESHOLD)
        )
        task.connectionTimeoutSeconds.set(
            pipelineSpec.nupokatiClientConnectionTimeout.convention(DEFAULT_NUPOKATI_CLIENT_CONNECTION_TIMEOUT)
        )
        task.readTimeoutSeconds.set(
            pipelineSpec.nupokatiClientReadTimeout.convention(DEFAULT_NUPOKATI_CLIENT_READ_TIMEOUT)
        )
        task.writeTimeoutSeconds.set(
            pipelineSpec.nupokatiClientWriteTimeout.convention(DEFAULT_NUPOKATI_CLIENT_WRITE_TIMEOUT)
        )
        task.useTls.set(pipelineSpec.useTls.orElse(true))

        val tlsCreds = TlsConfigurationPlugin.provideCredentialsService(project)
        task.tlsCredentialsService.set(tlsCreds)
        task.usesService(tlsCreds)

        task.statsDConfig.set(project.statsdConfig)
    }
}
