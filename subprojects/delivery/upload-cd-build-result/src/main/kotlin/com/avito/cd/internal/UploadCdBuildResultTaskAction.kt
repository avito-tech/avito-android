package com.avito.cd.internal

import com.avito.cd.BuildOutput
import com.avito.cd.CdBuildConfig
import com.avito.cd.CdBuildResult
import com.avito.git.GitState
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger

internal class UploadCdBuildResultTaskAction(
    private val client: OkHttpClient,
    private val gson: Gson,
    private val logger: Logger,
    private val suppressErrors: Boolean
) {
    fun send(
        testResults: CdBuildResult.TestResultsLink,
        buildOutput: BuildOutput,
        cdBuildConfig: CdBuildConfig,
        versionCode: String,
        teamcityUrl: String,
        gitState: GitState
    ) {
        val result = CdBuildResult(
            schemaVersion = cdBuildConfig.schemaVersion,
            buildNumber = versionCode,
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
        val request = Request.Builder()
            .url(cdBuildConfig.outputDescriptor.path)
            .put(cdBuildResultRaw.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            if (suppressErrors) {
                logger.error("Upload build result failed: ${response.code} ${response.body}")
            } else {
                throw RuntimeException("Upload build result failed: ${response.code} ${response.body}")
            }
        }
    }
}
