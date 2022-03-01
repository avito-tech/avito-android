package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.RegistryCredentials
import java.util.logging.Logger

internal class RegistryLoginImpl(
    private val docker: Docker,
    val credentials: RegistryCredentials
) : RegistryLogin {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    override fun login() {
        val result = docker.login(
            credentials.username,
            credentials.password,
            credentials.registry
        )
        check(result.isSuccess) {
            "Login to registry by user ${credentials.username} failed: ${result.exceptionOrNull()}"
        }
        val registryDescription = credentials.registry ?: "DockerHub"

        log.info("Logged in to registry $registryDescription as ${credentials.username}")
    }
}
