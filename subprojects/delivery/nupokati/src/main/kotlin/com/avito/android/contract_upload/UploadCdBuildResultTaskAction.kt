package com.avito.android.contract_upload

import com.avito.android.http.ArtifactoryClient
import com.avito.android.model.BuildOutput
import com.avito.android.model.CdBuildConfig
import com.avito.android.model.CdBuildResult
import com.avito.git.GitState
import com.google.gson.Gson
import org.gradle.api.logging.Logger

internal class UploadCdBuildResultTaskAction(
    private val client: ArtifactoryClient,
    private val gson: Gson,
    private val logger: Logger,
) {

    fun send(
        testResults: CdBuildResult.TestResultsLink,
        buildOutput: BuildOutput,
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
            artifacts = buildOutput.artifacts
        )
        val cdBuildResultRaw = gson.toJson(result)

        logger.lifecycle("CONTENT = $cdBuildResultRaw")

        val response = client.uploadJson(cdBuildConfig.outputDescriptor.path, cdBuildResultRaw)

        if (!response.isSuccessful) {
            throw RuntimeException("Upload build result failed: ${response.code} ${response.body?.string()}")
        }
    }
}
