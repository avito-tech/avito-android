package com.avito.android.artifacts_upload

import com.avito.android.gradle_configuration.extension.ArtifactV4
import com.avito.android.http.nupokati.NupokatiV4ClientBuildService
import com.avito.android.model.input.config.CdBuildConfig
import com.avito.android.model.input.config.CdBuildConfigV4
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

internal abstract class UploadArtifactsTask : DefaultTask() {

    @get:Internal
    internal abstract val nupokatiClientService: Property<NupokatiV4ClientBuildService>

    @get:Input
    internal abstract val artifacts: ListProperty<ArtifactV4>

    @get:Input
    internal abstract val appVersionCode: Property<Int>

    @get:Input
    internal abstract val cdBuildConfig: Property<CdBuildConfig>

    @TaskAction
    internal fun uploadArtifactsToNupokati() {
        val config = cdBuildConfig.get()
        require(config is CdBuildConfigV4) {
            "UploadArtifactsTask supported only for CdBuildConfig with schema version 4"
        }

        val client = nupokatiClientService.get().getClient()

        for (artifact in artifacts.get()) {
            val storeName: String? = when (artifact) {
                is ArtifactV4.AppBinary -> artifact.store.get()
                is ArtifactV4.Artifact -> null
            }
            client.uploadArtifact(
                project = config.project,
                platform = "android",
                version = config.releaseVersion,
                buildNumber = appVersionCode.get(),
                storeName = storeName,
                file = artifact.file.get(),
            ).fold(
                onSuccess = { logger.lifecycle("Artifact ${artifact.file} uploaded successfully") },
                onFailure = { throwable ->
                    logger.error("Failed to upload artifact ${artifact.file}", throwable)
                    throw throwable
                }
            )
        }
    }
}
