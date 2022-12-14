package ru.avito.image_builder.internal.command.emulator

import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.ImageId
import java.util.logging.Logger

internal class EmulatorTester(
    private val docker: Docker,
) {

    private val log: Logger = Logger.getLogger(this::class.java.simpleName)

    fun testEmulator(image: ImageId) {
        ensureHasAdb(image)
        ensureHasEmulator(image)
    }

    private fun ensureHasAdb(imageId: ImageId) {
        val result = docker.run(
            "--rm",
            "--device", "/dev/kvm",
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
            "--device", "/dev/kvm",
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
}
