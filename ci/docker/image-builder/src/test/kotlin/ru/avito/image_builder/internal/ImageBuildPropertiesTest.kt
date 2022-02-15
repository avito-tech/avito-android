package ru.avito.image_builder.internal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ImageBuildPropertiesTest {

    private lateinit var tmpDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        tmpDir = tempDir.toFile()
    }

    @Test
    fun `read from directory without dockerfile - fails`() {
        assertThrows<Exception> {
            ImageBuildProperties.readFromDir(tmpDir)
        }
    }

    @Test
    fun `no properties file - fails`() {
        givenDockerfile()

        assertThrows<Exception> {
            ImageBuildProperties.readFromDir(tmpDir)
        }
    }

    @Test
    fun `properties file without required fields - fails`() {
        givenDockerfile()
        givenPropertiesFile("")

        assertThrows<Exception> {
            ImageBuildProperties.readFromDir(tmpDir)
        }
    }

    @Test
    fun `has properties file - reads properties`() {
        givenDockerfile()
        givenPropertiesFile("""
            image-name=test-image
        """.trimIndent())

        val buildProperties = ImageBuildProperties.readFromDir(tmpDir)

        assertEquals("test-image", buildProperties.name)
    }

    private fun givenPropertiesFile(content: String) {
        File(tmpDir, "docker-build.properties").apply {
            writeText(content)
        }
    }

    private fun givenDockerfile() {
        File(tmpDir, "Dockerfile").apply {
            writeText("FROM busybox")
        }
    }
}
