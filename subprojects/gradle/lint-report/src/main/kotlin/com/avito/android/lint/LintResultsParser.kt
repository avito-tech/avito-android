package com.avito.android.lint

import com.avito.android.lint.model.LintIssue
import com.avito.android.lint.model.LintReportModel
import com.avito.utils.logging.CILogger
import java.io.File
import java.io.InputStream
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory

class LintResultsParser(private val log: CILogger) {

    fun parse(
        projectPath: String,
        lintXml: File,
        lintHtml: File
    ): LintReportModel {

        return try {
            val issues = readXmlLintReport(lintXml.inputStream())
            LintReportModel.Valid(projectPath, lintHtml, issues)
        } catch (error: UnsupportedFormatVersion) {
            throw error
        } catch (error: Exception) {
            log.info("Invalid lint report: ", error)
            LintReportModel.Invalid(projectPath, lintHtml, error)
        }
    }

    private data class MutableLintIssue(
        var id: String = "",
        var summary: String = "",
        var message: String = "",
        var severity: LintIssue.Severity = LintIssue.Severity.UNKNOWN,
        var path: String = "",
        var line: Int = 0
    )

    private fun readXmlLintReport(report: InputStream): List<LintIssue> {
        val xmlInputFactory = XMLInputFactory.newInstance()
        val issues = mutableListOf<LintIssue>()
        val reader = xmlInputFactory.createXMLEventReader(report)
        try {
            var issue: MutableLintIssue? = null

            while (reader.hasNext()) {
                val event = reader.nextEvent()

                if (event.isStartElement) {
                    val startElement = event.asStartElement()

                    if (startElement.name.localPart == "issues") {
                        val formatVersion = startElement.getAttributeByName(QName("format")).value
                        if (formatVersion != supportedFormatVersion) {
                            throw UnsupportedFormatVersion("Lint xml report for version $formatVersion is not supported")
                        }
                    }

                    if (startElement.name.localPart == "issue") {
                        issue = MutableLintIssue()
                        issue.id = requireNotNull(startElement.getAttributeByName(QName("id"))?.value)
                        issue.summary = requireNotNull(startElement.getAttributeByName(QName("summary"))?.value)
                        issue.message = startElement.getAttributeByName(QName("message"))?.value ?: "No message"
                        issue.severity = when (startElement.getAttributeByName(QName("severity"))?.value) {
                            "Error" -> LintIssue.Severity.ERROR
                            "Warning" -> LintIssue.Severity.WARNING
                            else -> LintIssue.Severity.UNKNOWN
                        }
                    }

                    if (startElement.name.localPart == "location") {
                        issue!!.path =
                            requireNotNull(startElement.getAttributeByName(QName("file"))?.value) { "Lint issue must have a file path" }

                        issue.line = startElement.getAttributeByName(QName("line"))?.value?.toInt() ?: 0
                    }
                }

                if (event.isEndElement) {
                    val endElement = event.asEndElement()
                    if (endElement.name.localPart == "issue") {
                        issues.add(
                            LintIssue(
                                id = issue!!.id,
                                summary = issue.summary,
                                message = issue.message,
                                severity = issue.severity,
                                line = issue.line,
                                path = issue.path.replace("../", "")
                            )
                        )
                    }
                }
            }
        } finally {
            reader.close()
        }
        return issues
    }
}

internal class UnsupportedFormatVersion(message: String) : RuntimeException(message)

private const val supportedFormatVersion = "5"
