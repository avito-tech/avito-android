package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.Image
import java.util.logging.Logger

internal class ImagePublisher(
    private val docker: Docker,
    private val builder: ImageBuilder,
    private val login: RegistryLogin,
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

        val result = docker.push(imageName)
        require(result.isSuccess) {
            "Failed to push the image: ${result.exceptionOrNull()}"
        }
        log.info("Published the image $imageName")
    }
}
