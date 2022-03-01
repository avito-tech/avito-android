package ru.avito.image_builder.internal.command

import java.util.logging.Logger

internal class NoOpRegistryLogin(
    val message: String? = null
) : RegistryLogin {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    override fun login() {
        log.info("Skipping login to Docker registry. ${message.orEmpty()}")
    }
}
