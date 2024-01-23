package com.avito.android.tech_budget.internal.warnings.report.converter

import com.avito.android.tech_budget.internal.warnings.report.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import com.avito.android.tech_budget.parser.FileParser
import com.avito.android.tech_budget.warnings.CompilerIssue
import org.gradle.api.file.FileCollection

internal class IssueToWarningConverter(
    private val fileParser: FileParser<CompilerIssue>
) {

    fun convert(projectIssues: Map<ProjectInfo, FileCollection>): List<Warning> {
        return projectIssues.entries.flatMap { (projectInfo, reportFiles) ->
            reportFiles
                .flatMap(fileParser::parse)
                .map { issue ->
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
