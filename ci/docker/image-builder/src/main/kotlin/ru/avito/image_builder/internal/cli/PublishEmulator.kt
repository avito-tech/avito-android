package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.EmulatorImageBuilder
import ru.avito.image_builder.internal.command.EmulatorType
import ru.avito.image_builder.internal.command.ImagePublisher
import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.command.RegistryLoginImpl
import ru.avito.image_builder.internal.command.emulator.EmulatorPreparer
import ru.avito.image_builder.internal.command.emulator.EmulatorTester
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

    private val type: EmulatorType by option(
        type = ArgType.Choice<EmulatorType>(),
        description = "Emulator type"
    ).required()

    override fun execute() {
        val docker = CliDocker()

        val builder = EmulatorImageBuilder(
            docker = docker,
            dockerfilePath = dockerfilePath,
            buildDir = File(buildDir),
            api = api,
            type = type,
            registry = registry,
            imageName = imageName,
            artifactoryUrl = artifactoryUrl,
            login = dockerHubLogin(docker),
            tagger = ImageTagger(docker),
            emulatorPreparer = EmulatorPreparer(docker, EmulatorTester(docker))
        )
        ImagePublisher(
            docker = docker,
            builder = builder,
            login = RegistryLoginImpl(
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
