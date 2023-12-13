package com.avito.android.network_contracts.scheme.fixation.collect.collector

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ApiSchemesCollectorTest {

    @Test
    fun `when codegen toml exists and schemes valid - return collected schemes as deps path`(
        @TempDir projectDir: File
    ) {
        val apiSchemesCollector = ApiSchemesCollector("avito:test:impl")
        val codegenFile = File(projectDir, "codegen.toml").createIfAbsent()
        val schemes = listOf(
            "api/test/scheme.yaml",
            "api/path.yaml",
        )

        val (schemesDir, schemesFiles) = generateSchemes(projectDir, schemes)

        schemes.forEach { schemeFilePath ->
            File(schemesDir, schemeFilePath).createIfAbsent()
        }

        val collectedSchemes = apiSchemesCollector.collect(codegenFile, schemesDir)
        assertThat(collectedSchemes).hasSize(schemes.size)
        assertThat(collectedSchemes.keys).containsExactlyElementsIn(schemes.map { "deps/avito_test_impl/$it" })
        assertThat(collectedSchemes.values).containsExactlyElementsIn(schemesFiles)
    }

    @Test
    fun `when codegen toml does not exists - return empty map`() {
        val apiSchemesCollector = ApiSchemesCollector("avito:test:impl")
        val codegenFile = File("codegen.toml")

        val collectedSchemes = apiSchemesCollector.collect(codegenFile, File("api"))
        assertThat(collectedSchemes).isEmpty()
    }

    private fun generateSchemes(projectDir: File, schemes: List<String>): Pair<File, List<File>> {
        val schemesDir = File(projectDir, "api-clients")
        val schemesFiles = schemes.map { schemeFilePath ->
            File(schemesDir, schemeFilePath).createIfAbsent()
        }
        return schemesDir to schemesFiles
    }
}

private fun File.createIfAbsent(): File {
    parentFile.mkdirs()
    if (!exists()) {
        createNewFile()
    }
    return this
}
