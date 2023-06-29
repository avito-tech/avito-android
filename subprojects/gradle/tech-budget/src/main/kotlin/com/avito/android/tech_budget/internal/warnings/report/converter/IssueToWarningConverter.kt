package com.avito.android.tech_budget.internal.warnings.report.converter

import com.avito.android.tech_budget.internal.warnings.report.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import com.avito.android.tech_budget.parser.FileParser
import com.avito.android.tech_budget.warnings.CompilerIssue
import org.gradle.api.file.RegularFileProperty

internal class IssueToWarningConverter(
    private val fileParser: FileParser<CompilerIssue>
) {

    fun convert(projectIssues: Map<ProjectInfo, RegularFileProperty>): List<Warning> {
        return projectIssues.entries.flatMap { (projectInfo, reportFile) ->
            val issues = fileParser.parse(reportFile.get().asFile)
            issues.map { issue ->
                Warning(
                    location = issue.location,
                    moduleName = projectInfo.path,
                    message = issue.message,
                    groupID = issue.group,
                    ruleID = issue.rule,
                    debt = issue.debt,
                )
            }
        }
    }
}
