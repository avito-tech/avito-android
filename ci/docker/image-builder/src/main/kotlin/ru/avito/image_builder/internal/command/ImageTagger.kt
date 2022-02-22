package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.Image
import ru.avito.image_builder.internal.docker.ImageId
import java.util.logging.Logger

internal class ImageTagger(
    private val docker: Docker
) {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    fun tag(id: ImageId, name: String): Image {
        // Image ID is different in registries and can't be used as unique and stable identifier
        // https://github.com/distribution/distribution/issues/1662#issuecomment-213079540
        // Thus, we fixing it as an unique tag
        @Suppress("UnnecessaryVariable")
        val tag = id.value.take(12)

        val result = docker.tag(id.value, "$name:$tag")
        check(result.isSuccess) {
            "Unable to tag image ${id.value}: ${result.exceptionOrNull()}"
        }

        log.info("Image $id tagged as $name:$tag")
        return Image(id, name, tag)
    }
}
