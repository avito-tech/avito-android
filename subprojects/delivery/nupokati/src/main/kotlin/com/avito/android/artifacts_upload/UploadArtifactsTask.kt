package com.avito.android.artifacts_upload

import com.avito.android.http.nupokati.NupokatiV4ClientTask
import com.avito.android.model.input.config.CdBuildConfigV4
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

internal abstract class UploadArtifactsTask : NupokatiV4ClientTask() {

    @get:Input
    internal abstract val storeNames: MapProperty</* fileName: */ String, /* storeName: */ String>

    @get:InputFiles
    internal abstract val artifacts: ConfigurableFileCollection

    @get:Input
    internal abstract val appVersionCode: Property<Int>

    @get:Input
    internal abstract val cdBuildConfig: Property<CdBuildConfigV4>

    @TaskAction
    internal fun uploadArtifactsToNupokati() {
        val config = cdBuildConfig.get()
        val client = buildNupokatiClient()

        artifacts.forEach { artifact ->
            val storeName = storeNames.get()[artifact.name]

            client.uploadArtifact(
                project = config.project,
                platform = "android",
                version = config.releaseVersion,
                buildNumber = appVersionCode.get(),
                storeName = storeName,
                file = artifact,
            ).fold(
                onSuccess = { logger.lifecycle("Artifact ${artifact.name} uploaded successfully") },
                onFailure = { throwable ->
                    logger.error("Failed to upload artifact ${artifact.name}", throwable)
                    throw throwable
                }
            )
        }
    }
}
