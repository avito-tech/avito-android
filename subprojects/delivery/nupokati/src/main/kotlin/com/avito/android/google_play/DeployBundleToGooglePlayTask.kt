package com.avito.android.google_play

import com.avito.android.model.CdBuildConfig
import com.avito.upload_to_googleplay.GooglePlayDeploy
import com.avito.upload_to_googleplay.createGooglePlayDeployer
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public abstract class DeployBundleToGooglePlayTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @get:InputFile
    public val googlePlayKeyFile: Property<RegularFile> = objects.fileProperty()

    @get:InputFile
    public val bundle: Property<RegularFile> = objects.fileProperty()

    @get:InputFile
    public val mapping: Property<RegularFile> = objects.fileProperty()

    @get:Input
    public abstract val track: Property<CdBuildConfig.Deployment.Track>

    @get:Input
    public abstract val applicationId: Property<String>

    @TaskAction
    public fun doWork() {
        val deployer = createGooglePlayDeployer(
            jsonKey = googlePlayKeyFile.get().asFile,
            logger = logger
        )

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
