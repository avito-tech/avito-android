package com.avito.android.artifactory_backup

import com.avito.android.http.ArtifactoryClient
import com.avito.android.http.createArtifactoryHttpClient
import com.avito.android.model.input.Deployment
import com.avito.android.model.input.DeploymentV2
import com.avito.android.stats.StatsDConfig
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class ArtifactoryBackupTask : DefaultTask() {

    @get:Input
    public abstract val artifactoryUser: Property<String>

    @get:Input
    public abstract val artifactoryPassword: Property<String>

    /**
     * full url to artifactory directory
     * example: http://<artifactory-host>/artifactory/mobile-releases/avito_android/118.0_2/
     */
    @get:Input
    public abstract val artifactoryUploadPath: Property<String>

    @Deprecated("included in Artifact for schema version >= 3")
    @get:Input
    public abstract val buildConfiguration: Property<String>

    @get:Input
    public abstract val schemaVersion: Property<Long>

    @Deprecated("use artifacts for schema version >= 3")
    @get:InputFiles
    public abstract val files: Property<FileCollection>

    @get:Input
    internal abstract val artifacts: ListProperty<Deployment>

    @get:Internal
    public abstract val statsDConfig: Property<StatsDConfig>

    @get:OutputFile
    public abstract val buildOutput: RegularFileProperty

    @TaskAction
    public fun doWork() {
        val loggerFactory = GradleLoggerPlugin.getLoggerFactory(this).get()

        val httpClient = createArtifactoryHttpClient(
            user = artifactoryUser.get(),
            password = artifactoryPassword.get(),
            statsDConfig = statsDConfig.get()
        )

        val artifactoryClient = ArtifactoryClient(httpClient)

        @Suppress("UNCHECKED_CAST")
        val artifactsFactory: ArtifactsFactory<Deployment> = when (schemaVersion.get()) {
            2L -> ArtifactsV2Factory() as ArtifactsFactory<Deployment>
            3L -> ArtifactsV3Factory() as ArtifactsFactory<Deployment>
            else -> throw IllegalArgumentException("Unsupported schema version: ${schemaVersion.get()}")
        }

        val artifactsAdapter = ArtifactsAdapter(schemaVersion = schemaVersion.get())
        val artifactoryBackupAction = ArtifactoryBackupAction(
            artifactoryClient = artifactoryClient,
            artifactsFactory = artifactsFactory,
            artifactsAdapter = artifactsAdapter,
            loggerFactory = loggerFactory
        )

        @Suppress("DEPRECATION")
        val deployments: List<Deployment> = when (schemaVersion.get()) {
            2L -> files.map { it.map { file -> DeploymentV2(file, buildConfiguration.get()) } }.get()
            3L -> artifacts.get()
            else -> throw IllegalArgumentException("Unsupported schema version: ${schemaVersion.get()}")
        }

        val artifactsJson = artifactoryBackupAction.backup(
            artifactoryUploadPath = artifactoryUploadPath.get(),
            deployments = deployments
        )

        this.buildOutput.get().asFile.writeText(artifactsJson)
    }
}
