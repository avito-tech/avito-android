package com.avito.android.model.input.v4

import com.avito.android.model.input.config.CdBuildConfigV4
import com.avito.android.model.input.config.parser.CdBuildConfigParser
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class CdBuildConfigV4ParserTest {

    private lateinit var testProjectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        testProjectDir = tempPath.toFile()
    }

    @Test
    fun `parse valid config - all fields present`() {
        val config = parseConfig(
            """
            {
                "schema_version": 4,
                "project": "avito",
                "release_version": "129.0",
                "skip_upload": false
            }
            """.trimIndent()
        )

        val expected = CdBuildConfigV4(
            schemaVersion = 4L,
            project = "avito",
            releaseVersion = "129.0",
            skipUpload = false
        )

        assertThat(config).isEqualTo(expected)
    }

    @Test
    fun `parse valid config - ignores unknown fields`() {
        val config = parseConfig(
            """
            {
                "schema_version": 4,
                "project": "avito",
                "release_version": "129.0",
                "skip_upload": false,
                "unknown_field": "should be ignored"
            }
            """.trimIndent()
        )

        val expected = CdBuildConfigV4(
            schemaVersion = 4L,
            project = "avito",
            releaseVersion = "129.0",
            skipUpload = false
        )

        assertThat(config).isEqualTo(expected)
    }

    @Test
    fun `parse fails - wrong schema version`() {
        val exception = assertThrows<IllegalArgumentException> {
            parseConfig(
                """
                {
                    "schema_version": 3,
                    "project": "avito",
                    "release_version": "129.0",
                    "skip_upload": false
                }
                """.trimIndent()
            )
        }

        assertThat(exception.message).contains("Unsupported schema version: 3")
        assertThat(exception.message).contains("Required: 4")
    }

    @Test
    fun `parse fails - config file does not exist`() {
        val nonExistentFile = File(testProjectDir, "non-existent.json")

        val exception = assertThrows<IllegalArgumentException> {
            CdBuildConfigParser.parseCdBuildConfigV4(nonExistentFile)
        }

        assertThat(exception.message).contains("Can't find cd config file")
    }

    private fun parseConfig(config: String): CdBuildConfigV4 {
        val inputFile = File(testProjectDir, "config.json")
        inputFile.writeText(config)

        return CdBuildConfigParser.parseCdBuildConfigV4(inputFile)
    }
}
