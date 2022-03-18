package ru.avito.image_builder.internal.cli

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import ru.avito.image_builder.internal.command.ImageTagger
import ru.avito.image_builder.internal.command.NoOpRegistryLogin
import ru.avito.image_builder.internal.command.RegistryLogin
import ru.avito.image_builder.internal.command.RegistryLoginImpl
import ru.avito.image_builder.internal.command.SimpleImageBuilder
import ru.avito.image_builder.internal.docker.CliDocker
import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.RegistryCredentials
import java.io.File

@OptIn(ExperimentalCli::class)
internal open class BuildImage(
    name: String,
    description: String
) : Subcommand(name, description) {

    protected val buildDir: String by option(
        type = ArgType.String,
        description = "Path to mounted directory with Dockerfile"
    ).required()

    protected val dockerHubUsername: String? by option(
        type = ArgType.String,
        description = "DockerHub username"
    )

    protected val dockerHubPassword: String? by option(
        type = ArgType.String,
        description = "DockerHub password"
    )

    protected val registry: String by option(
        type = ArgType.String,
        description = "Docker target registry"
    ).required()

    protected val artifactoryUrl: String by option(
        type = ArgType.String,
        description = "Internal artifactory url"
    ).required()

    protected val imageName: String by option(
        type = ArgType.String,
        description = "Image name. Usually, it's in format <repository>/<image-name> (e.g. 'android/image-builder')"
    ).required()

    override fun execute() {
        imageBuilder().build()
    }

    private fun imageBuilder(): SimpleImageBuilder {
        val docker = CliDocker()

        return SimpleImageBuilder(
            docker = docker,
            buildDir = File(buildDir),
            login = dockerHubLogin(docker),
            tagger = ImageTagger(docker),
            registry = registry,
            artifactoryUrl = artifactoryUrl,
            imageName = imageName,
        )
    }

    protected fun dockerHubLogin(docker: Docker): RegistryLogin {
        val username = dockerHubUsername
        val password = dockerHubPassword

        return if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            RegistryLoginImpl(
                docker = docker,
                credentials = RegistryCredentials(
                    registry = null,
                    username = username,
                    password = password,
                )
            )
        } else {
            NoOpRegistryLogin(
                message = "No dockerHubUsername and dockerHubPassword setup."
            )
        }
    }
}
