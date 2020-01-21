package com.avito.android.lint.report

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ReportXmlAdapterTest {

    private lateinit var tempDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        this.tempDir = tempDir.toFile()
    }

    @Test
    fun `deserialize serialized data`() {
        val issues = listOf<LintIssue>(
            UnusedDependency(Severity.warning, message = "message", summary = "not used"),
            RedundantDependency(Severity.error, message = "message", summary = "transitive dependency")
        )
        val report = DependenciesReport(issues)
        val file = createTempFile(directory = tempDir)

        ReportXmlAdapter().write(report, file)
        val deserialized = ReportXmlAdapter().read(file)

        assertThat(deserialized.issues).isEqualTo(issues)
    }

}
