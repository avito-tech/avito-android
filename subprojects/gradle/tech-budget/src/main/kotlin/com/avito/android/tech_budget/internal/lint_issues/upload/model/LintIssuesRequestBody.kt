package com.avito.android.tech_budget.internal.lint_issues.upload.model

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.squareup.moshi.JsonClass

/*
http://stash.msk.avito.ru/projects/MAR/repos/service-tech-budget/browse/brief/rpc/service/dump_lint_issues.brief

message DumpLintIssuesIn {
    dumpInfo   DumpInfoIn             `Common dump info`
    lintIssues []LintIssueDumpPayload `List of feature toggles`
}
 */
@JsonClass(generateAdapter = true)
internal class LintIssuesRequestBody(
    val dumpInfo: DumpInfo,
    val lintIssues: List<LintIssue>
)
