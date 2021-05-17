package com.avito.android.lint.model

import com.avito.android.lint.internal.model.LintReportModel
import com.avito.android.lint.internal.model.LintResultsParser
import com.avito.android.lint.internal.model.Severity
import com.avito.android.lint.internal.model.UnsupportedFormatVersion
import com.avito.logger.StubLoggerFactory
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

@Suppress("UnstableApiUsage")
internal class LintResultsParserTest {

    private lateinit var tempDir: File

    private val loggerFactory = StubLoggerFactory

    @BeforeEach
    fun setup(@TempDir dir: Path) {
        tempDir = dir.toFile()
    }

    @Test
    fun `report contains expected issues`() {
        val model = parse(
            xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <issues by="lint 4.1.2" format="5">
            
                <issue
                    id="GoogleAppIndexingWarning"
                    severity="Information"
                    message="App is not indexable ..."
                    category="Usability"
                    priority="5"
                    summary="Missing support for Firebase App Indexing"
                    explanation="Adds URLs to ..."
                    url="https://g.co/AppIndexing/AndroidStudio"
                    urls="https://g.co/AppIndexing/AndroidStudio"
                    errorLine1="    &lt;application"
                    errorLine2="    ^">
                    <location
                        file="/app/src/main/AndroidManifest.xml"
                        line="2"
                        column="5"/>
                </issue>

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
                
                <issue
                    id="MissingDefaultResource"
                    severity="Fatal"
                    message="The plurals ..."
                    category="Correctness"
                    priority="6"
                    summary="Missing Default"
                    explanation="If a resource ..."
                    errorLine1="..."
                    errorLine2="...">
                    <location
                        file="/app/src/main/res/values-ru/plurals.xml"
                        line="1"
                        column="1"/>
                </issue>
            </issues>
            """.trimIndent()
        )

        assertThat(model is LintReportModel.Valid)
        model as LintReportModel.Valid
        assertThat(model.issues).hasSize(4)

        model.issues[0].apply {
            assertThat(severity).isEqualTo(Severity.INFORMATIONAL)
            assertThat(message).isEqualTo("App is not indexable ...")
            assertThat(path).isEqualTo("/app/src/main/AndroidManifest.xml")
            assertThat(line).isEqualTo(2)
        }
        model.issues[1].apply {
            assertThat(severity).isEqualTo(Severity.WARNING)
            assertThat(message).isEqualTo("message")
            assertThat(path).isEqualTo("/full/path")
            assertThat(line).isEqualTo(0)
        }
        model.issues[2].apply {
            assertThat(severity).isEqualTo(Severity.ERROR)
            assertThat(message).isEqualTo("...")
            assertThat(path).isEqualTo("/full/path/file.kt")
            assertThat(line).isEqualTo(1)
        }
        model.issues[3].apply {
            assertThat(severity).isEqualTo(Severity.FATAL)
            assertThat(message).isEqualTo("The plurals ...")
            assertThat(path).isEqualTo("/app/src/main/res/values-ru/plurals.xml")
            assertThat(line).isEqualTo(1)
        }
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
        assertThat(readingError).isInstanceOf<UnsupportedFormatVersion>()
        assertThat(readingError?.message).contains("Lint xml report for version 6 is not supported")
    }

    @Test
    fun `invalid report file - invalid model`() {
        val model = parse(
            xmlContent = "invalid xml file"
        )

        assertThat(model).isInstanceOf<LintReportModel.Invalid>()
    }

    private fun parse(
        xmlContent: String,
        htmlContent: String = "some html file content",
        projectPath: String = ":app"
    ): LintReportModel = LintResultsParser(loggerFactory = loggerFactory).parse(
        projectPath = projectPath,
        lintXml = File(tempDir, "lint-report.xml").apply { writeText(xmlContent) },
        lintHtml = File(tempDir, "lint-report.html").apply { writeText(htmlContent) }
    )
}
