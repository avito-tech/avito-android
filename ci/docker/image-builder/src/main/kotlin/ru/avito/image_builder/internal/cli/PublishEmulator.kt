package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.EmulatorImageBuilder
import ru.avito.image_builder.internal.command.ImagePublisher
import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.command.RegistryLogin
import ru.avito.image_builder.internal.docker.CliDocker
import ru.avito.image_builder.internal.docker.RegistryCredentials
import java.io.File

internal class PublishEmulator(
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

    private val api: Int by option(
        type = ArgType.Int,
        description = "API version"
    ).required()

    override fun execute() {
        val docker = CliDocker()

        val builder = EmulatorImageBuilder(
            docker = docker,
            buildDir = File(buildDir),
            api = api,
            registry = registry,
            imageName = imageName,
            login = RegistryLogin(
                docker = docker,
                credentials = RegistryCredentials(
                    registry = null,
                    username = dockerHubUsername,
                    password = dockerHubPassword,
                )
            ),
            tagger = ImageTagger(docker),
        )
        ImagePublisher(
            docker = docker,
            builder = builder,
            login = RegistryLogin(
                docker = docker,
                credentials = RegistryCredentials(
                    registry = registry,
                    username = registryUsername,
                    password = registryPassword,
                )
            )
        ).publish()
    }
}
