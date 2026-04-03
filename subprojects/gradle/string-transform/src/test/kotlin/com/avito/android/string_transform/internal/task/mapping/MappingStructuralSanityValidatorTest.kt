package com.avito.android.string_transform.internal.task.mapping

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class MappingStructuralSanityValidatorTest {

    private val validator = MappingStructuralSanityValidator()

    @Test
    fun `mapping structural sanity validator - succeeds - when mapping is parseable`(@TempDir dir: File) {
        val mappingFile = dir.resolve("mapping.txt").apply {
            writeText(VALID_MAPPING)
        }

        val result = validator.validate(mappingFile)

        assertThat(result.getOrThrow()).isEqualTo(Unit)
    }

    @Test
    fun `mapping structural sanity validator - fails - when mapping is not parseable`(@TempDir dir: File) {
        val mappingFile = dir.resolve("mapping.txt").apply {
            writeText("not a mapping")
        }

        val result = validator.validate(mappingFile)

        val failure = result.fold(
            onSuccess = { error("Expected validation failure") },
            onFailure = { it },
        )
        assertThat(failure).isInstanceOf(IllegalStateException::class.java)
        assertThat(failure.message).contains("Transformed mapping failed structural sanity validation")
    }

    @Test
    fun `mapping structural sanity validator - succeeds - when file is empty`(@TempDir dir: File) {
        val mappingFile = dir.resolve("mapping.txt").apply {
            writeText("")
        }

        val result = validator.validate(mappingFile)

        assertThat(result.getOrThrow()).isEqualTo(Unit)
    }

    @Test
    fun `mapping structural sanity validator - succeeds - when file contains only comments`(@TempDir dir: File) {
        val mappingFile = dir.resolve("mapping.txt").apply {
            writeText(
                """
                # compiler: R8
                # pg_map_id: abc123
                """.trimIndent()
            )
        }

        val result = validator.validate(mappingFile)

        assertThat(result.getOrThrow()).isEqualTo(Unit)
    }

    @Test
    fun `mapping structural sanity validator - fails - when line shape is invalid`(@TempDir dir: File) {
        val mappingFile = dir.resolve("mapping.txt").apply {
            writeText(
                """
                # compiler: R8
                com.example.samplevalue.Holder => a:
                """.trimIndent()
            )
        }

        val result = validator.validate(mappingFile)

        val failure = result.fold(
            onSuccess = { error("Expected validation failure") },
            onFailure = { it },
        )
        assertThat(failure).isInstanceOf(IllegalStateException::class.java)
        assertThat(failure.message).contains("Transformed mapping failed structural sanity validation")
        assertThat(failure.cause?.message).contains(
            "Transformed mapping contains lines outside the supported proguard mapping shape"
        )
    }

    private companion object {
        private const val VALID_MAPPING = """
            # compiler: R8
            com.example.samplevalue.Holder -> a:
                java.lang.String samplevalueField -> a
                void samplevalueMethod(java.lang.String) -> a
        """
    }
}
