package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.docker.CliDocker
import ru.avito.image_builder.internal.docker.ContainerId
import ru.avito.image_builder.internal.docker.Image
import ru.avito.image_builder.internal.docker.ImageId
import java.io.File
import java.util.logging.Logger

internal class EmulatorImageBuilder(
    private val docker: CliDocker,
    private val buildDir: File,
    private val api: Int,
    private val registry: String,
    private val imageName: String,
    private val login: RegistryLogin,
    private val tagger: ImageTagger,
) : ImageBuilder {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    override fun build(): Image {
        login.login()

        val imageId = buildImage()
        val preparedImageId = prepareEmulator(imageId)
        testEmulator(preparedImageId)

        return tag(preparedImageId)
    }

    private fun prepareEmulator(imageId: ImageId): ImageId {
        val containerId = runContainer(imageId)

        prepareEmulator(containerId)

        val preparedImageId = commitChanges(containerId)

        removeContainer(containerId)

        return preparedImageId
    }

    private fun buildImage(): ImageId {
        log.info("Building an image ...")

        val emulatorArch = if (api < 28) "x86" else "x86_64"

        val buildResult = docker.build(
            buildDir.canonicalPath,
            "--build-arg", "SDK_VERSION=$api",
            "--build-arg", "EMULATOR_ARCH=$emulatorArch",
        )
        check(buildResult.isSuccess) {
            "Failed to build the image: ${buildResult.exceptionOrNull()}"
        }
        val id = buildResult.getOrThrow()
        log.info("Built image: $id")
        return id
    }

    private fun runContainer(imageId: ImageId): ContainerId {
        val runResult = docker.run(
            "--rm",
            "-d",
            "--privileged",
            "--entrypoint", "sleep",
            imageId.value,
            "1h"
        )
        check(runResult.isSuccess) {
            "Failed to run the image: ${runResult.exceptionOrNull()}"
        }
        val containerId = runResult.getOrThrow()
        log.info("Run container $containerId")
        return ContainerId(containerId)
    }

    private fun prepareEmulator(containerId: ContainerId) {
        val result = docker.exec(containerId.value, "bash", "-c", "VERSION=$api ./prepare_snapshot.sh")
        check(result.isSuccess) {
            "Failed to exec preparation script: ${result.exceptionOrNull()}"
        }
        val output = result.getOrThrow()
        check(output.contains("Emulator preparation finished")) {
            """
                |Couldn't find message about finished preparation. 
                |Last messages: ${output.takeLast(2048)}
            """.trimMargin()
        }
    }

    private fun commitChanges(containerId: ContainerId): ImageId {
        log.info("Committing changed container ...")
        val commitResult = docker.commit(
            change = "ENTRYPOINT [\"./entrypoint.sh\"]",
            container = containerId.value
        )
        check(commitResult.isSuccess) {
            "Failed to commit container: ${commitResult.exceptionOrNull()}"
        }
        val imageId = commitResult.getOrThrow()
        log.info("Committed image: ${imageId.value}")
        return imageId
    }

    private fun removeContainer(containerId: ContainerId) {
        val result = docker.remove("--force", container = containerId.value)
        check(result.isSuccess) {
            "Failed to remove container ${containerId.value}: ${result.exceptionOrNull()}"
        }
    }

    private fun testEmulator(image: ImageId) {
        ensureHasAdb(image)
        ensureHasEmulator(image)
    }

    private fun ensureHasAdb(imageId: ImageId) {
        val result = docker.run(
            "--rm",
            "--privileged",
            "--entrypoint", "adb",
            imageId.value,
            "version"
        )
        check(result.isSuccess) {
            "Can't run adb in emulator image: ${result.exceptionOrNull()}"
        }
        val output = result.getOrThrow()
        check(output.contains("Android Debug Bridge version")) {
            "adb returned unexpected output: $output"
        }
        log.info("Image ${imageId.value} has adb")
    }

    private fun ensureHasEmulator(imageId: ImageId) {
        val result = docker.run(
            "--rm",
            "--privileged",
            "--entrypoint", "avdmanager",
            imageId.value,
            "list", "avd"
        )
        check(result.isSuccess) {
            "Can't run avdmanager in emulator image: ${result.exceptionOrNull()}"
        }
        val output = result.getOrThrow()
        check(output.contains("emulator_")) {
            "avdmanager returned unexpected output: $output"
        }
        log.info("Image ${imageId.value} has Android Virtual Device")
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
