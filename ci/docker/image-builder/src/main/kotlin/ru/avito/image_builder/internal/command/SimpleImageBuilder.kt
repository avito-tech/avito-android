package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.Image
import ru.avito.image_builder.internal.docker.ImageId
import java.io.File
import java.util.logging.Logger

internal class SimpleImageBuilder(
    private val docker: Docker,
    /**
     * Relative path to the Dockerfile inside context (buildDir)
     */
    private val dockerfilePath: String,
    /**
     * build path
     * https://docs.docker.com/engine/reference/commandline/build/#build-with-path
     */
    private val buildDir: File,
    private val login: RegistryLogin,
    private val tagger: ImageTagger,
    private val registry: String?,
    private val artifactoryUrl: String?,
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

        val buildArgs = mutableListOf("--file", File(buildDir, dockerfilePath).canonicalPath, buildDir.canonicalPath)

        if (registry.isNullOrBlank()) {
            log.warning("--registry not specified, make sure base images are available locally or in dockerHub")
        } else {
            buildArgs.add("--build-arg")
            buildArgs.add("DOCKER_REGISTRY=$registry")
        }

        if (artifactoryUrl.isNullOrBlank()) {
            log.warning("--artifactoryUrl not specified")
        } else {
            buildArgs.add("--build-arg")
            buildArgs.add("DOCKER_REGISTRY=$registry")
        }

        val buildResult = docker.build(*buildArgs.toTypedArray())
        check(buildResult.isSuccess) {
            "Failed to build the image: ${buildResult.exceptionOrNull()}"
        }
        val id = buildResult.getOrThrow()
        log.info("Built image: $id")
        return id
    }

    private fun tag(id: ImageId): Image =
        tagger.tag(id, buildString {
            if (!registry.isNullOrBlank()) {
                append(registry)
                append('/')
            }
            append(imageName)
        })
}
