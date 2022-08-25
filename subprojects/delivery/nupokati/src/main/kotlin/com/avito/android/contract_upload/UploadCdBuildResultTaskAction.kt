package com.avito.android.contract_upload

import com.avito.android.http.ArtifactoryClient
import com.avito.android.model.input.CdBuildConfig
import com.avito.android.model.output.CdBuildResult
import com.avito.git.GitState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal class UploadCdBuildResultTaskAction(private val client: ArtifactoryClient) {

    fun send(
        testResults: CdBuildResult.TestResultsLink,
        artifacts: JsonElement,
        cdBuildConfig: CdBuildConfig,
        versionCode: Int,
        teamcityUrl: String,
        gitState: GitState
    ) {
        val result = CdBuildResult(
            schemaVersion = cdBuildConfig.schemaVersion,
            buildNumber = versionCode.toString(),
            releaseVersion = cdBuildConfig.releaseVersion,
            teamcityBuildUrl = teamcityUrl,
            gitBranch = CdBuildResult.GitBranch(
                name = gitState.currentBranch.name,
                commitHash = gitState.currentBranch.commit
            ),
            testResults = testResults,
            artifacts = artifacts
        )

        val jsonString = Json.encodeToString(result)

        val response = client.uploadJson(
            url = cdBuildConfig.outputDescriptor.path,
            fileContent = jsonString
        )

        if (!response.isSuccessful) {
            throw RuntimeException("Upload build result failed: ${response.code} ${response.body?.string()}")
        }
    }
}
