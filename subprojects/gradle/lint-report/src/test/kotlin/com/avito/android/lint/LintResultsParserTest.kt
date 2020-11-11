package com.avito.android.lint

import com.avito.android.lint.model.LintIssue
import com.avito.android.lint.model.LintReportModel
import com.avito.utils.logging.FakeCILogger
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class LintResultsParserTest {

    private lateinit var tempDir: File

    private val logger = FakeCILogger()

    @BeforeEach
    fun setup(@TempDir dir: Path) {
        tempDir = dir.toFile()
    }

    @Test
    fun `report with warnings and errors contains expected issues`() {
        val model = parse(
            xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <issues by="lint 3.3.2" format="5">

                <issue
                    id="ObsoleteLintCustomCheck"
                    severity="Warning"
                    category="Lint"
                    message="message"
                    priority="10"
                    summary="Obsolete custom lint check"
                    explanation="explanation">

                    <location file="/full/path" />
                </issue>

                <issue
                    id="Recycle"
                    severity="Error"
                    message="..."
                    category="Performance"
                    priority="9"
                    summary="..."
                    explanation="..."
                    errorLine1="..."
                    errorLine2="...">
                    <location
                        file="/full/path/file.kt"
                        line="1"
                        column="2"/>
                </issue>
            </issues>
            """.trimIndent()
        )

        assertThat(model is LintReportModel.Valid)
        model as LintReportModel.Valid
        assertThat(model.issues).hasSize(2)
        assertThat(model.issues[0].severity).isEqualTo(LintIssue.Severity.WARNING)
        assertThat(model.issues[0].message).isEqualTo("message")
        assertThat(model.issues[0].path).isEqualTo("/full/path")
        assertThat(model.issues[0].line).isEqualTo(0)
        assertThat(model.issues[1].severity).isEqualTo(LintIssue.Severity.ERROR)
        assertThat(model.issues[1].message).isEqualTo("...")
        assertThat(model.issues[1].path).isEqualTo("/full/path/file.kt")
        assertThat(model.issues[1].line).isEqualTo(1)
    }

    @Test
    fun `report with unsupported format version - must fail with explanation`() {
        var readingError: Exception? = null
        try {
            parse(
                xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <issues format="6" by="lint 4.0.0">
            </issues>
            """.trimIndent()
            )
        } catch (error: Exception) {
            readingError = error
        }
        assertThat(readingError).isNotNull()
        assertThat(readingError).isInstanceOf(UnsupportedFormatVersion::class.java)
        assertThat(readingError?.message).contains("Lint xml report for version 6 is not supported")
    }

    @Test
    fun `invalid report file - invalid model`() {
        val model = parse(
            xmlContent = "invalid xml file"
        )

        assertThat(model).isInstanceOf(LintReportModel.Invalid::class.java)
    }

    private fun parse(
        xmlContent: String,
        htmlContent: String = "some html file content",
        projectPath: String = ":app"
    ): LintReportModel = LintResultsParser(logger).parse(
        projectPath = projectPath,
        lintXml = File(tempDir, "lint-report.xml").apply { writeText(xmlContent) },
        lintHtml = File(tempDir, "lint-report.html").apply { writeText(htmlContent) }
    )
}
