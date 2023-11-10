package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.ImageBuilder
import ru.avito.image_builder.internal.command.ImagePublisher
import ru.avito.image_builder.internal.command.RegistryLogin
import ru.avito.image_builder.internal.command.RegistryType
import ru.avito.image_builder.internal.docker.CliDocker
import java.time.Duration

internal abstract class BaseEmceeBuildImage(
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

    private val publishRegistryType: RegistryType by option(
        type = ArgType.Choice<RegistryType>(),
        description = "Registry for publishing"
    ).required()

    protected val imageRegistryTagName: String by option(
        type = ArgType.String,
        description = "Docker image registry name which will be used in image tag name"
    ).required()

    protected val imageVersionTag: String? by option(
        type = ArgType.String,
        description = "Docker image version tag. Default value is a shorten image id"
    )

    protected fun publishImage(docker: CliDocker, imageBuilder: ImageBuilder) {
        ImagePublisher(
            docker = docker,
            builder = imageBuilder,
            login = getPublishingRegistryLogin(docker),
            publishTimeout = Duration.ofMinutes(20)
        ).publish()
    }

    private fun getPublishingRegistryLogin(docker: CliDocker): RegistryLogin {
        return when (publishRegistryType) {
            RegistryType.DOCKER_HUB -> dockerHubLogin(docker)
            RegistryType.CONFIGURED -> configuredRegistryLogin(docker, registryUsername, registryPassword)
        }
    }
}
