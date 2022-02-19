package ru.avito.image_builder.internal

import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.Image
import java.io.File
import java.util.logging.Logger

internal class ImageBuilder(
    private val docker: Docker,
    private val buildDir: File,
    private val login: RegistryLogin,
    private val registry: String,
    private val imageName: String,
) {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    fun build(): Image {
        login.login()

        val id = buildImage()

        return tag(id)
    }

    private fun buildImage(): String {
        log.info("Building an image ...")

        val buildResult = docker.build(buildDir.canonicalPath)
        check(buildResult.isSuccess) {
            "Failed to build the image: ${buildResult.exceptionOrNull()}"
        }
        val id = buildResult.getOrThrow()
        log.info("Built image: $id")
        return id
    }

    private fun tag(id: String): Image {
        val name = "$registry/$imageName"
        @Suppress("UnnecessaryVariable")
        val tag = id

        docker.tag(id, "$name:$tag")

        log.info("Image $id tagged as $name:$tag")
        return Image(id, name, tag)
    }
}
