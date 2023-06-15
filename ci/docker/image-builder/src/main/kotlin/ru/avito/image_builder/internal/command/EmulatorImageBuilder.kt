package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.command.emulator.EmulatorPreparer
import ru.avito.image_builder.internal.docker.CliDocker
import ru.avito.image_builder.internal.docker.Image
import ru.avito.image_builder.internal.docker.ImageId
import java.io.File
import java.util.logging.Logger

internal class EmulatorImageBuilder(
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
    private val api: Int,
    private val registry: String,
    private val imageName: String,
    private val artifactoryUrl: String,
    private val login: RegistryLogin,
    private val tagger: ImageTagger,
    private val emulatorPreparer: EmulatorPreparer,
) : ImageBuilder {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    override fun build(): Image {
        login.login()

        val imageId = buildImage()
        val preparedImageId = emulatorPreparer.prepareEmulators(
            imageId = imageId,
            apis = setOf(api),
            emulatorLocale = "en-US"
        )

        return tag(preparedImageId)
    }

    private fun buildImage(): ImageId {
        log.info("Building an image ...")

        val emulatorArch = if (api < 28) "x86" else "x86_64"

        val buildResult = docker.build(
            "--build-arg", "DOCKER_REGISTRY=$registry",
            "--build-arg", "SDK_VERSION=$api",
            "--build-arg", "EMULATOR_ARCH=$emulatorArch",
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

    private fun tag(id: ImageId): Image {
        val name = if (imageName.endsWith("-$api")) {
            imageName
        } else {
            "$imageName-$api"
        }
        return tagger.tag(id, "$registry/$name")
    }
}
