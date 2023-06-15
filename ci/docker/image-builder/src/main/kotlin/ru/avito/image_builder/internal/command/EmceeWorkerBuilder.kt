package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.command.emulator.EmulatorPreparer
import ru.avito.image_builder.internal.docker.CliDocker
import ru.avito.image_builder.internal.docker.Image
import ru.avito.image_builder.internal.docker.ImageId
import java.io.File
import java.util.logging.Logger

internal class EmceeWorkerBuilder(
    private val docker: CliDocker,
    /**
     * Relative path to the Dockerfile inside context (buildDir)
     */
    private val dockerfilePath: String,
    /**
     * build path
     * https://docs.docker.com/engine/reference/commandline/build/#build-with-path
     */
    private val buildDir: File,
    private val apis: Set<Int>,
    private val registry: String,
    private val imageName: String,
    private val artifactoryUrl: String,
    private val login: RegistryLogin,
    private val tagger: ImageTagger,
    private val emulatorPreparer: EmulatorPreparer,
    private val emulatorLocale: String
) : ImageBuilder {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    override fun build(): Image {
        login.login()

        val imageId = buildImage()
        val preparedImageId = emulatorPreparer.prepareEmulators(
            imageId = imageId,
            apis = apis,
            emulatorLocale = emulatorLocale
        )

        return tag(preparedImageId)
    }

    private fun buildImage(): ImageId {
        log.info("Building an image ...")

        val buildResult = docker.build(
            "--build-arg", "DOCKER_REGISTRY=$registry",
            "--build-arg", "SDK_VERSIONS=${apis.joinToString(separator = " ")}",
            "--build-arg", "ARTIFACTORY_URL=$artifactoryUrl",
            "--file", File(buildDir, dockerfilePath).canonicalPath,
            buildDir.canonicalPath,
        )
        check(buildResult.isSuccess) {
            "Failed to build the image: ${buildResult.exceptionOrNull()}"
        }
        val id = buildResult.getOrThrow()
        log.info("Built image: $id")
        return id
    }

    private fun tag(id: ImageId): Image = tagger.tag(id, "$registry/$imageName")
}
