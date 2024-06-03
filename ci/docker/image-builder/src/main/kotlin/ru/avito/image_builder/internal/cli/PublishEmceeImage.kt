package ru.avito.image_builder.internal.cli

import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.command.NoOpRegistryLogin
import ru.avito.image_builder.internal.command.SimpleImageBuilder
import ru.avito.image_builder.internal.docker.CliDocker
import java.io.File

internal class PublishEmceeImage(
    name: String,
    description: String,
) : BaseEmceeBuildImage(name, description) {

    override fun execute() {
        val docker = CliDocker()

        val builder = SimpleImageBuilder(
            docker = docker,
            dockerfilePath = dockerfilePath,
            buildDir = File(buildDir),
            login = NoOpRegistryLogin("Disabled because of the DockerHub ban"),
            tagger = ImageTagger(docker, imageVersionTag),
            registry = registry,
            imageRegistryTagName = imageRegistryTagName,
            artifactoryUrl = artifactoryUrl,
            imageName = imageName,
        )
        buildOrPublishImage(docker, builder)
    }
}
