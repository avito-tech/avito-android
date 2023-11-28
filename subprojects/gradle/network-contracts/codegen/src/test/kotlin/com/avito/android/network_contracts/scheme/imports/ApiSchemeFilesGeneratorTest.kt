package com.avito.android.network_contracts.scheme.imports

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ApiSchemeFilesGeneratorTest {

    @Test
    fun `valid schema - generate files with expected structure`(@TempDir directory: File) {
        val schema = listOf(errorsSchema, modelSchema)
        val apiSchemeFilesGenerator = ApiSchemesFilesGenerator(directory)
        val generatedFiles = apiSchemeFilesGenerator.generateFiles(schemas = schema)

        assertThat(generatedFiles).hasSize(2)

        with(generatedFiles.first()) {
            val generatedErrorSchemaFile = this
            assertThat(generatedErrorSchemaFile.toRelativeString(directory))
                .isEqualTo(errorsSchema.path.removePrefix("/"))
            assertThat(generatedErrorSchemaFile.readText())
                .isEqualTo(errorsSchema.decodedContent)
        }

        with(generatedFiles.last()) {
            val generatedModelSchemaFile = this
            assertThat(generatedModelSchemaFile.toRelativeString(directory))
                .isEqualTo(modelSchema.path.removePrefix("/"))
            assertThat(generatedModelSchemaFile.readText())
                .isEqualTo(modelSchema.decodedContent)
        }
    }

    companion object
}
