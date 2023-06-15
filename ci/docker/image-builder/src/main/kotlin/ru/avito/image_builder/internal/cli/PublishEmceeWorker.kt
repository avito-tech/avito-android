package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.EmceeWorkerBuilder
import ru.avito.image_builder.internal.command.ImagePublisher
import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.command.RegistryLoginImpl
import ru.avito.image_builder.internal.command.emulator.EmulatorPreparer
import ru.avito.image_builder.internal.command.emulator.EmulatorTester
import ru.avito.image_builder.internal.docker.CliDocker
import ru.avito.image_builder.internal.docker.RegistryCredentials
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

    override fun execute() {
        val docker = CliDocker()

        val builder = EmceeWorkerBuilder(
            docker = docker,
            dockerfilePath = dockerfilePath,
            buildDir = File(buildDir),
            apis = apis.split(' ').mapNotNull { it.toIntOrNull() }.toSet(),
            registry = registry,
            imageName = imageName,
            artifactoryUrl = artifactoryUrl,
            login = dockerHubLogin(docker),
            tagger = ImageTagger(docker),
            emulatorPreparer = EmulatorPreparer(docker, EmulatorTester(docker)),
            emulatorLocale = emulatorLocale
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
