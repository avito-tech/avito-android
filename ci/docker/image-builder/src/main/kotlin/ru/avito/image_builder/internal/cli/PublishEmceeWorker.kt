package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.EmceeWorkerBuilder
import ru.avito.image_builder.internal.command.ImagePublisher
import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.command.RegistryLogin
import ru.avito.image_builder.internal.command.RegistryType
import ru.avito.image_builder.internal.command.emulator.EmulatorPreparer
import ru.avito.image_builder.internal.command.emulator.EmulatorTester
import ru.avito.image_builder.internal.docker.CliDocker
import java.io.File

internal class PublishEmceeWorker(
    name: String,
    description: String,
) : BuildImage(name, description) {

    private val registryUsername: String by option(
        type = ArgType.String,
        description = "Docker registry username"
    ).required()

    private val registryPassword: String by option(
        type = ArgType.String,
        description = "Docker registry password"
    ).required()

    private val apis: String by option(
        type = ArgType.String,
        description = "Space separated list of API versions"
    ).required()

    private val emulatorLocale: String by option(
        type = ArgType.String,
        description = "Emulator locale in BCP 47 format. en-US locale is default."
    ).required()

    private val publishRegistryType: RegistryType by option(
        type = ArgType.Choice<RegistryType>(),
        description = "Registry for publishing"
    ).required()

    private val imageRegistryTagName: String by option(
        type = ArgType.String,
        description = "Docker image registry name which will be used in image tag name"
    ).required()

    private val imageVersionTag: String? by option(
        type = ArgType.String,
        description = "Docker image version tag. Default value is a shorten image id"
    )

    override fun execute() {
        val docker = CliDocker()

        val builder = EmceeWorkerBuilder(
            docker = docker,
            dockerfilePath = dockerfilePath,
            buildDir = File(buildDir),
            apis = apis.split(' ').mapNotNull { it.toIntOrNull() }.toSet(),
            registry = registry,
            imageRegistryTagName = imageRegistryTagName,
            imageName = imageName,
            artifactoryUrl = artifactoryUrl,
            login = dockerHubLogin(docker),
            tagger = ImageTagger(docker, imageVersionTag),
            emulatorPreparer = EmulatorPreparer(docker, EmulatorTester(docker)),
            emulatorLocale = emulatorLocale
        )
        ImagePublisher(
            docker = docker,
            builder = builder,
            login = getPublishingRegistryLogin(docker)
        ).publish()
    }

    private fun getPublishingRegistryLogin(docker: CliDocker): RegistryLogin {
        return when (publishRegistryType) {
            RegistryType.DOCKER_HUB -> dockerHubLogin(docker)
            RegistryType.CONFIGURED -> configuredRegistryLogin(docker, registryUsername, registryPassword)
        }
    }
}
