package com.avito.cd

import com.avito.android.androidAppExtension
import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.utils.gradle.envArgs
import com.avito.utils.logging.ciLogger
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

//TODO internal?

const val uploadCdBuildResultTaskName = "uploadCdBuildResult"

abstract class UploadCdBuildResultTask
@Inject constructor(
    private val uiTestConfiguration: String,
    private val user: String,
    private val password: String,
    private val output: CdBuildConfig.OutputDescriptor,
    private val suppressErrors: Boolean
) : DefaultTask() {

    init {
        onlyIf {
            !output.skipUpload
        }
    }

    private val uploadAction by lazy {
        UploadCdBuildResultTaskAction(
            gson = Providers.gson,
            client = Providers.client(
                user = user,
                password = password,
                logger = object: HttpLoggingInterceptor.Logger  {
                    override fun log(message: String) {
                        project.ciLogger.info(message)
                    }
                }
            ),
            suppressErrors = suppressErrors
        )
    }

    @TaskAction
    fun sendCdBuildResult() {
        val gitState = project.gitState { project.ciLogger.info(it) }
        uploadAction.send(
            buildOutput = project.buildOutput.get(),
            cdBuildConfig = project.cdBuildConfig.get(),
            versionCode = project.androidAppExtension.defaultConfig.versionCode.toString(),
            teamcityUrl = project.envArgs.build.url,
            gitState = gitState.get(),
            uiTestConfiguration = uiTestConfiguration
        )
    }
}

internal class UploadCdBuildResultTaskAction(
    private val client: OkHttpClient,
    private val gson: Gson,
    private val suppressErrors: Boolean
) {
    fun send(
        buildOutput: BuildOutput,
        cdBuildConfig: CdBuildConfig,
        versionCode: String,
        teamcityUrl: String,
        gitState: GitState,
        uiTestConfiguration: String
    ) {
        val testResults = buildOutput.testResults[uiTestConfiguration]
        requireNotNull(testResults) {
            "Need $uiTestConfiguration testResults on buildOutput"
        }
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
        if (!suppressErrors && !response.isSuccessful) {
            throw RuntimeException("Upload build result failed: ${response.code} ${response.body.toString()}")
        }
    }
}
