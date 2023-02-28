package com.avito.android.tech_budget.internal.lint_issues.upload.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/*
http://stash.msk.avito.ru/projects/MAR/repos/service-tech-budget/browse/brief/rpc/service/dump_lint_issues.brief

message LintIssueDumpPayload {
    column     *int    `Column number that contains lint issue`
    line       *int    `Line number that contains lint issue`
    file       *string `File which contains lint issue`
    moduleName *string `Name of module, which contains lint issue`
    message     string `Full displayable lint issue message`
    ruleID      string `ID of the linter rule produce in the issue`
    severity    string `Severity of the lint issue`
}
 */
@JsonClass(generateAdapter = true)
internal data class LintIssue(
    @Json(name = "column") val issueFileColumn: Int?,
    @Json(name = "line") val issueFileLine: Int?,
    @Json(name = "file") val issueFileName: String?,
    @Json(name = "moduleName") val moduleName: String,
    @Json(name = "ruleID") val ruleId: String,
    @Json(name = "message") val message: String,
    @Json(name = "severity") val severity: String,
)
