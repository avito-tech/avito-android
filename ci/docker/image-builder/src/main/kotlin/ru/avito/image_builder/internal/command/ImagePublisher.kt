package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.Image
import ru.avito.image_builder.internal.docker.LONG_OPERATION_TIMEOUT
import java.time.Duration
import java.util.logging.Logger

internal class ImagePublisher(
    private val docker: Docker,
    private val builder: ImageBuilder,
    private val login: RegistryLogin,
    private val publishTimeout: Duration = LONG_OPERATION_TIMEOUT
) {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    fun publish() {
        login.login()

        val image = builder.build()

        publish(image)
    }

    private fun publish(image: Image) {
        val imageName = "${image.name}:${image.tag}"
        log.info("Publishing an image $imageName ...")

        val result = docker.push(image.name, "--all-tags", timeout = publishTimeout)
        require(result.isSuccess) {
            "Failed to push the image: ${result.exceptionOrNull()}"
        }
        log.info("Published the image $imageName")
    }
}
