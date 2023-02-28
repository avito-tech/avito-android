package com.avito.android.tech_budget.internal.lint_issues.upload

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.lint_issues.upload.model.LintIssuesRequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadLintIssuesApi {

    @POST("dumpLintIssues/")
    fun dumpLintIssues(@Body request: LintIssuesRequestBody): Call<DumpResponse>
}
