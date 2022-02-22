package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.Image
import ru.avito.image_builder.internal.docker.ImageId
import java.io.File
import java.util.logging.Logger

internal class SimpleImageBuilder(
    private val docker: Docker,
    private val buildDir: File,
    private val login: RegistryLogin,
    private val tagger: ImageTagger,
    private val registry: String,
    private val imageName: String,
) : ImageBuilder {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    override fun build(): Image {
        login.login()

        val id = buildImage()

        return tag(id)
    }

    private fun buildImage(): ImageId {
        log.info("Building an image ...")

        val buildResult = docker.build(buildDir.canonicalPath)
        check(buildResult.isSuccess) {
            "Failed to build the image: ${buildResult.exceptionOrNull()}"
        }
        val id = buildResult.getOrThrow()
        log.info("Built image: $id")
        return id
    }

    private fun tag(id: ImageId): Image =
        tagger.tag(id, "$registry/$imageName")
}
