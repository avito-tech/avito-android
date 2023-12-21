package com.avito.android.network_contracts.validation.analyzer.rules

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CorruptedFilesDiagnosticRuleTest {

    @Test
    fun `when generated files are not equals - return analyze problem`(@TempDir projectDir: File) {
        val generatedDir = File(projectDir, "generated")
        val referencesDir = File(projectDir, "references")

        val referenceFile = File(referencesDir, "file.txt").generateWith("reference")
        val generatedFile = File(generatedDir, referenceFile.name).generateWith("generated")

        val rule = CorruptedFilesDiagnosticRule(
            generatedFilesDir = generatedDir,
            referencesFilesDir = referencesDir,
        )

        rule.analyze()
        assertThat(rule.findings).isNotEmpty()

        val diagnostic = rule.findings.first()
        assertThat(diagnostic.message).contains(generatedFile.path)
    }

    @Test
    fun `when generated files contains file not in references - return analyze problem`(@TempDir projectDir: File) {
        val generatedDir = File(projectDir, "generated")
        val referencesDir = File(projectDir, "references")

        File(referencesDir, "file.txt").generateWith("reference")
        val generatedRedundantFile = File(generatedDir, "folder/file.txt").generateWith("redundant")

        val rule = CorruptedFilesDiagnosticRule(
            generatedFilesDir = generatedDir,
            referencesFilesDir = referencesDir,
        )

        rule.analyze()

        assertThat(rule.findings).isNotEmpty()

        val diagnostic = rule.findings.first()
        assertThat(diagnostic.message).contains(generatedRedundantFile.path)
    }

    @Test
    fun `when generated files are equals - return empty analyze problems`(@TempDir projectDir: File) {
        val generatedDir = File(projectDir, "generated")
        val referencesDir = File(projectDir, "references")

        File(referencesDir, "file.txt").generateWith("generated")
        File(generatedDir, "file.txt").generateWith("generated")

        val rule = CorruptedFilesDiagnosticRule(
            generatedFilesDir = generatedDir,
            referencesFilesDir = referencesDir,
        )
        rule.analyze()
        assertThat(rule.findings).isEmpty()
    }

    @Test
    fun `when generated and references files are empty - return empty analyze problems`(@TempDir projectDir: File) {
        val generatedDir = File(projectDir, "generated")
        val referencesDir = File(projectDir, "references")

        val rule = CorruptedFilesDiagnosticRule(
            generatedFilesDir = generatedDir,
            referencesFilesDir = referencesDir,
        )
        rule.analyze()
        assertThat(rule.findings).isEmpty()
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
