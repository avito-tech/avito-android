package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.EmulatorImageBuilder
import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.docker.CliDocker
import java.io.File

internal class BuildEmulator(
    name: String,
    description: String,
) : BuildImage(name, description) {

    private val api: Int by option(
        type = ArgType.Int,
        description = "API version"
    ).required()

    override fun execute() {
        val docker = CliDocker()

        EmulatorImageBuilder(
            docker = docker,
            dockerfilePath = dockerfilePath,
            buildDir = File(buildDir),
            api = api,
            registry = registry,
            imageName = imageName,
            artifactoryUrl = artifactoryUrl,
            login = dockerHubLogin(docker),
            tagger = ImageTagger(docker),
        ).build()
    }
}
