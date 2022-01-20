package com.avito.android.google_play

import com.avito.android.model.CdBuildConfig
import com.avito.upload_to_googleplay.GooglePlayDeploy
import com.avito.upload_to_googleplay.GooglePlayDeployerFactoryProducer
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

public abstract class DeployBundleToGooglePlayTask : DefaultTask() {

    @get:InputFile
    public abstract val googlePlayKeyFile: RegularFileProperty

    @get:InputFile
    public abstract val bundle: RegularFileProperty

    @get:InputFile
    public abstract val mapping: RegularFileProperty

    @get:Input
    public abstract val track: Property<CdBuildConfig.Deployment.Track>

    @get:Input
    public abstract val applicationId: Property<String>

    @get:Optional
    @get:Input
    public abstract val mockGooglePlayUrl: Property<String>

    @TaskAction
    public fun doWork() {

        val deployer = GooglePlayDeployerFactoryProducer.create(
            jsonKey = googlePlayKeyFile.get().asFile,
            mockWebServerUrl = mockGooglePlayUrl.orNull,
        ).create(logger = logger)

        deployer.deploy(
            listOf(
                GooglePlayDeploy(
                    binary = bundle.get().asFile,
                    binaryType = GooglePlayDeploy.BinaryType.BUNDLE,
                    track = track.get().name,
                    applicationId = applicationId.get(),
                    mapping = mapping.get().asFile
                )
            )
        )
    }
}
