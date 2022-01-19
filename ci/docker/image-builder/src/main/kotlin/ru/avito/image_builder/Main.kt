package ru.avito.image_builder

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import ru.avito.image_builder.internal.ImageBuilder
import ru.avito.image_builder.internal.ImagePublisher
import ru.avito.image_builder.internal.RegistryCredentials
import ru.avito.image_builder.internal.RegistryLogin
import ru.avito.image_builder.internal.docker.CliDocker
import java.io.File

public object Main {

    @OptIn(ExperimentalCli::class)
    internal open class BuildImage(
        name: String,
        description: String
    ) : Subcommand(name, description) {

        protected val buildDir: String by option(
            type = ArgType.String,
            description = "Path to mounted directory with Dockerfile"
        ).required()

        protected val dockerHubUsername: String by option(
            type = ArgType.String,
            description = "DockerHub username"
        ).required()

        protected val dockerHubPassword: String by option(
            type = ArgType.String,
            description = "DockerHub password"
        ).required()

        protected val registry: String by option(
            type = ArgType.String,
            description = "Docker target registry"
        ).required()

        override fun execute() {
            imageBuilder().build()
        }

        protected fun imageBuilder(): ImageBuilder {
            val docker = CliDocker()

            return ImageBuilder(
                docker = docker,
                buildDir = File(buildDir),
                login = RegistryLogin(
                    docker = docker,
                    credentials = RegistryCredentials(
                        registry = null,
                        username = dockerHubUsername,
                        password = dockerHubPassword,
                    )
                ),
                registry = registry
            )
        }
    }

    @OptIn(ExperimentalCli::class)
    internal class PublishImage(
        name: String,
        description: String
    ) : BuildImage(name, description) {

        private val registryUsername: String by option(
            type = ArgType.String,
            description = "Docker registry username"
        ).required()

        private val registryPassword: String by option(
            type = ArgType.String,
            description = "Docker registry password"
        ).required()

        override fun execute() {
            val docker = CliDocker()

            ImagePublisher(
                docker = CliDocker(),
                builder = imageBuilder(),
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

    @OptIn(ExperimentalCli::class)
    @JvmStatic
    public fun main(args: Array<String>) {
        val parser = ArgParser(
            programName = "image-builder",
        )

        parser.subcommands(
            BuildImage("build", "Build image"),
            PublishImage("publish", "Build and publish image")
        )
        parser.parse(sanitizeEmptyArgs(args))
    }

    private fun sanitizeEmptyArgs(args: Array<String>): Array<String> {
        return if (args.isEmpty()) {
            arrayOf("--help")
        } else {
            args
        }
    }
}
