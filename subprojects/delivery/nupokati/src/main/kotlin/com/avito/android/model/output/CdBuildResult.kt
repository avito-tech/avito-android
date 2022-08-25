package com.avito.android.model.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class CdBuildResult(
    @SerialName("schema_version") val schemaVersion: Long,
    @SerialName("teamcity_build_url") val teamcityBuildUrl: String,
    @SerialName("build_number") val buildNumber: String,
    @SerialName("release_version") val releaseVersion: String,
    @SerialName("git_branch") val gitBranch: GitBranch,
    @SerialName("test_results") val testResults: TestResultsLink,
    val artifacts: JsonElement
) {

    @Serializable
    data class GitBranch(
        val name: String,
        @SerialName("commit_hash") val commitHash: String
    )

    @Serializable
    data class TestResultsLink(
        @SerialName("report_url") val reportUrl: String,
        @SerialName("report_coordinates") val reportCoordinates: ReportCoordinates
    ) {

        @Serializable
        data class ReportCoordinates(
            @SerialName("plan_slug") val planSlug: String,
            @SerialName("job_slug") val jobSlug: String,
            @SerialName("run_id") val runId: String
        )
    }
}
