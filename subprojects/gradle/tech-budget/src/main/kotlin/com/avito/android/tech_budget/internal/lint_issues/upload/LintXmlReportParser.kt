package com.avito.android.tech_budget.internal.lint_issues.upload

import com.avito.android.tech_budget.internal.lint_issues.upload.model.LintIssue
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import java.io.File

internal class LintXmlReportParser(
    private val projectDir: String,
) {
    fun parseXmlReport(xmlFiles: Collection<File>): List<LintIssue> {
        return xmlFiles
            .flatMap(::parseXmlReportAsSequence)
            .distinct()
            .toList()
    }

    private fun parseXmlReportAsSequence(xmlFile: File): Sequence<LintIssue> {
        val xml = XmlParser().parse(xmlFile)
        val issues = xml["issue"] as NodeList
        return issues.iterator()
            .asSequence()
            .filterIsInstance<Node>()
            .map { issue ->
                val issueLocation = (issue["location"] as NodeList).first() as Node
                val issueFilePath = issueLocation.attribute("file") as String
                LintIssue(
                    moduleName = getModuleNameByPath(issueFilePath, projectDir),
                    ruleId = issue.attribute("id") as String,
                    severity = issue.attribute("severity") as String,
                    message = issue.attribute("message") as String,
                    issueFileColumn = (issueLocation.attribute("column") as? String)?.toInt(),
                    issueFileLine = (issueLocation.attribute("line") as? String)?.toInt(),
                    issueFileName = issueFilePath.substring(projectDir.length)
                )
            }
    }

    companion object {
        internal fun getModuleNameByPath(issueFilePath: String, projectDir: String): String {
            val localPath = issueFilePath.substring(projectDir.length)

            var lastIndex = localPath.indexOf("/src/main")
            if (lastIndex == -1) {
                lastIndex = localPath.lastIndexOf('/')
                if (lastIndex != -1 && !localPath.substring(lastIndex).contains('.')) {
                    lastIndex = localPath.length
                }
            }

            return if (lastIndex != -1) {
                localPath
                    .substring(0, lastIndex)
                    .trimEnd { it == '/' }
                    .replace('/', ':')
            } else {
                "unknown"
            }
        }
    }
}
