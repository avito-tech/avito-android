package com.avito.android.lint.report

import com.avito.utils.createOrClear
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

internal class ReportXmlAdapter {

    private val documentBuilder: DocumentBuilder by lazy {
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
    }

    fun read(file: File): DependenciesReport {
        require(file.exists()) { "File ${file.path} exists" }
        require(file.canRead()) { "File ${file.path} is readable" }

        val document = documentBuilder.parse(file)
        val issueElements = document.getElementsByTagName("issue")
        val issues = issueElements.toIterable()
            .map { readIssue(it) }

        return DependenciesReport(issues)
    }

    private fun readIssue(node: Node): LintIssue {
        val id = node.attributes.getNamedItem("id").nodeValue.orEmpty()
        return when (id) {
            UNUSED_DEPENDENCY_ID -> readUnusedDependency(node)
            REDUNDANT_DEPENDENCY_ID -> readRedundantDependency(node)
            else -> throw RuntimeException("Unknown issue type: $id")
        }
    }

    private fun readUnusedDependency(node: Node): UnusedDependency {
        return UnusedDependency(
            severity = readSeverity(node),
            message = readMessage(node),
            summary = readSummary(node)
        )
    }

    private fun readSeverity(node: Node): Severity {
        val stringValue = node.readStringAttribute("severity")
        return Severity.values().firstOrNull { it.name == stringValue }
            ?: throw IllegalArgumentException("Unknown severity: $stringValue")
    }

    private fun readRedundantDependency(node: Node): RedundantDependency {
        return RedundantDependency(
            severity = readSeverity(node),
            message = readMessage(node),
            summary = readSummary(node)
        )
    }

    private fun readMessage(node: Node) = node.readStringAttribute("message")

    private fun readSummary(node: Node) = node.readStringAttribute("summary")

    private fun Node.readStringAttribute(attribute: String): String {
        return attributes.getNamedItem(attribute).nodeValue.orEmpty()
    }

    fun write(report: DependenciesReport, file: File) {
        file.createOrClear()

        file.writer().use { writer ->
            writer.appendln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            writer.appendln("<issues>")

            report.issues.forEach { issue ->
                writer.appendln("""
                <issue
                    id="${issue.id}"
                    severity="${issue.severity.name}"
                    category="Lint"
                    message="${issue.message}"
                    summary="${issue.summary}"/>
            """.trimIndent())
            }
            writer.appendln("</issues>")
        }
    }
}

fun NodeList.toIterable(): Iterable<Node> {
    val out = ArrayList<Node>(length)
    for (i in 0 until length) out.add(item(i))
    return out
}
