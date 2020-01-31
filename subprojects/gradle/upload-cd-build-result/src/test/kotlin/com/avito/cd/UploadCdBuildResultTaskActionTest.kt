package com.avito.cd

import com.avito.git.Branch
import com.avito.git.GitStateStub
import com.avito.test.http.MockDispatcher
import okhttp3.Credentials
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UploadCdBuildResultTaskActionTest {

    private val mockWebServer = MockWebServer()
    private val dispatcher = MockDispatcher()
    private val gson = Providers.gson
    val user = "user"
    val password = "password"
    val outputPath = "file_path"
    private val action = UploadCdBuildResultTaskAction(
        gson = Providers.gson,
        client = Providers.client(
            user = user,
            password = password
        )
    )

    @BeforeEach
    fun setup() {
        mockWebServer.setDispatcher(dispatcher)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `cd build result successful sended`() {
        val schemaVersion: Long = 2
        val releaseVersion = "249.0"
        val teamcityUrl = "xxx/viewLog.html?buildId=100&tab=buildLog"
        val versionCode = "100"
        val gitBranch = CdBuildResult.GitBranch(
            name = "branchName",
            commitHash = "commitHash"
        )
        val gitState = GitStateStub(
            currentBranch = Branch(
                gitBranch.name,
                gitBranch.commitHash
            )
        )
        val uiTestConfiguration = "regression"
        val testResults = mapOf(
            uiTestConfiguration to CdBuildResult.TestResults(
                reportId = "123",
                reportUrl = "no matter url",
                reportCoordinates = CdBuildResult.TestResults.ReportCoordinates(
                    planSlug = "2",
                    jobSlug = "3",
                    runId = "4"
                )
            )
        )
        val artifacts = listOf(
            CdBuildResult.Artifact.AndroidBinary(
                "apk",
                "release.apk",
                "${mockWebServer.url("")}/apps-release-local/appA-android/appA/1-1-100/release.apk",
                BuildVariant.RELEASE
            ),
            CdBuildResult.Artifact.FileArtifact(
                "featureToggles",
                "1.json",
                "${mockWebServer.url("")}/apps-release-local/appA-android/appA/1-1-100/1.json"
            )
        )

        val cdBuildConfig = CdBuildConfig(
            schemaVersion = schemaVersion,
            project = NupokatiProject.AVITO,
            releaseVersion = releaseVersion,
            outputDescriptor = CdBuildConfig.OutputDescriptor("${mockWebServer.url("")}$outputPath", true),
            deployments = emptyList()
        )

        val expected = CdBuildResult(
            schemaVersion = schemaVersion,
            teamcityBuildUrl = teamcityUrl,
            releaseVersion = releaseVersion,
            buildNumber = versionCode,
            testResults = testResults.getValue(uiTestConfiguration),
            artifacts = artifacts,
            gitBranch = gitBranch
        )

        val sendOutputRequest = dispatcher.captureRequest {
            method.contains("PUT") && path == "/$outputPath" && getHeader("Content-Type").startsWith("application/json")
        }

        action.send(
            buildOutput = BuildOutput().apply {
                this.artifacts = artifacts
                this.testResults.putAll(testResults)
            },
            cdBuildConfig = cdBuildConfig,
            versionCode = versionCode,
            teamcityUrl = teamcityUrl,
            gitState = gitState,
            uiTestConfiguration = uiTestConfiguration
        )

        sendOutputRequest
            .checks
            .singleRequestCaptured()
            .bodyContains(gson.toJson(expected))
            .containsHeader("Authorization", Credentials.basic(user, password))
    }
}
