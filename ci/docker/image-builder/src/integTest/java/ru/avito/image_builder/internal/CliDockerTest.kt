package ru.avito.image_builder.internal

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import ru.avito.image_builder.internal.docker.CliDocker
import ru.avito.image_builder.internal.docker.Docker
import ru.avito.image_builder.internal.docker.ImageId
import java.io.File
import java.nio.file.Path
import java.time.Duration

internal class CliDockerTest {

    private lateinit var tmpDir: File
    private lateinit var docker: Docker

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        tmpDir = tempDir.toFile()
        docker = CliDocker()
    }

    @Test
    fun `build - success`() {
        buildImage()
    }

    @Test
    fun `build - fail`() {
        val error = assertThrows<Throwable> {
            buildImage(
                dockerfile = "UNKNOWN_INVALID_INSTRUCTION"
            )
        }
        assertTrue(error.message.orEmpty().contains("unknown instruction"), error.toString())
    }

    @Test
    fun `run - success`() {
        val id = buildImage()

        val result = docker.run(id.value, "echo", "message")

        assertTrue(result.isSuccess, result.toString())
        val output = result.getOrThrow()
        assertTrue(output.contains("message"), output)
    }

    @Test
    fun `exec - success`() {
        val imageId = buildImage()

        val runResult = docker.run("-d", "--entrypoint", "sleep", imageId.value, "1m")
        assertTrue(runResult.isSuccess, runResult.toString())

        val containerId = runResult.getOrThrow()
        val execResult = docker.exec(containerId, "sh", "-c", "echo message")
        assertTrue(execResult.isSuccess, execResult.toString())

        val output = execResult.getOrThrow()
        assertTrue(output.contains("message"), output)
    }

    private fun buildImage(
        dockerfile: String = "FROM busybox:1.34.1"
    ): ImageId {
        givenDockerfile(tmpDir, dockerfile)

        val buildResult = docker.build(
            tmpDir.absolutePath,
            "--no-cache",
            timeout = Duration.ofMinutes(1)
        )
        assertTrue(buildResult.isSuccess, buildResult.toString())

        return buildResult.getOrThrow()
    }

    private fun givenDockerfile(dir: File, content: String) {
        File(dir, "Dockerfile").apply {
            writeText(content)
        }
    }
}
