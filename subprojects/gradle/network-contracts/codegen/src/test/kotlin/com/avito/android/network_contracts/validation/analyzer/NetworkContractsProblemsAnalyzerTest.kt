package com.avito.android.network_contracts.validation.analyzer

import com.avito.android.network_contracts.validation.diagnostic.NetworkContractsDiagnostic
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class NetworkContractsProblemsAnalyzerTest {

    @Test
    fun `when generated files are not equals - return analyze problem`(@TempDir projectDir: File) {
        val generatedDir = File(projectDir, "generated")
        val referencesDir = File(projectDir, "references")

        val referenceFile = File(referencesDir, "file.txt").generateWith("reference")
        val generatedFile = File(generatedDir, referenceFile.name).generateWith("generated")

        val analyzer = NetworkContractsProblemsAnalyzer(
            generatedFilesDir = generatedDir,
            referencesFilesDir = referencesDir,
        )

        val result = analyzer.analyze()
        assertThat(result).isNotEmpty()
        assertThat(result.first()).isInstanceOf(NetworkContractsDiagnostic.Failure::class.java)

        val diagnostic = result.first() as NetworkContractsDiagnostic.Failure
        assertThat(diagnostic.corruptedFilePaths).containsExactlyElementsIn(listOf(generatedFile.path))
    }

    @Test
    fun `when generated files contains file not in references - return analyze problem`(@TempDir projectDir: File) {
        val generatedDir = File(projectDir, "generated")
        val referencesDir = File(projectDir, "references")

        File(referencesDir, "file.txt").generateWith("reference")
        val generatedRedundantFile = File(generatedDir, "folder/file.txt").generateWith("redundant")

        val analyzer = NetworkContractsProblemsAnalyzer(
            generatedFilesDir = generatedDir,
            referencesFilesDir = referencesDir,
        )

        val result = analyzer.analyze()
        assertThat(result).isNotEmpty()
        assertThat(result.first()).isInstanceOf(NetworkContractsDiagnostic.Failure::class.java)

        val diagnostic = result.first() as NetworkContractsDiagnostic.Failure
        assertThat(diagnostic.corruptedFilePaths).containsExactlyElementsIn(listOf(generatedRedundantFile.path))
    }

    @Test
    fun `when generated files are equals - return empty analyze problems`(@TempDir projectDir: File) {
        val generatedDir = File(projectDir, "generated")
        val referencesDir = File(projectDir, "references")

        File(referencesDir, "file.txt").generateWith("generated")
        File(generatedDir, "file.txt").generateWith("generated")

        val analyzer = NetworkContractsProblemsAnalyzer(
            generatedFilesDir = generatedDir,
            referencesFilesDir = referencesDir,
        )
        assertThat(analyzer.analyze()).isEmpty()
    }

    @Test
    fun `when generated and references files are empty - return empty analyze problems`(@TempDir projectDir: File) {
        val generatedDir = File(projectDir, "generated")
        val referencesDir = File(projectDir, "references")

        val analyzer = NetworkContractsProblemsAnalyzer(
            generatedFilesDir = generatedDir,
            referencesFilesDir = referencesDir,
        )
        assertThat(analyzer.analyze()).isEmpty()
    }

    private fun File.generateWith(text: String): File {
        parentFile.mkdirs()
        if (!exists()) {
            createNewFile()
        }
        writeText(text)
        return this
    }
}
