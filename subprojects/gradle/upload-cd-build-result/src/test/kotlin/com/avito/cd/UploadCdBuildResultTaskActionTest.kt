package com.avito.cd

import com.avito.git.Branch
import com.avito.git.GitStateStub
import com.avito.test.http.MockDispatcher
import com.google.common.truth.Truth.assertThat
import okhttp3.Credentials
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UploadCdBuildResultTaskActionTest {

    private val mockWebServer = MockWebServer()
    private val dispatcher = MockDispatcher()
    private val gson = Providers.gson
    private val user = "user"
    private val password = "password"
    private val outputPath = "file_path"

    @BeforeEach
    fun setup() {
        mockWebServer.dispatcher = dispatcher
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `cd build result - was sent successfully`() {
        val expected = CdBuildResult(
            schemaVersion = schemaVersion,
            teamcityBuildUrl = teamcityUrl,
            releaseVersion = releaseVersion,
            buildNumber = versionCode,
            testResults = stubTestResults.getValue(uiTestConfiguration),
            artifacts = stubArtifacts,
            gitBranch = gitBranch
        )
        dispatcher.mockResponse({ true }, MockResponse().setResponseCode(200))

        val sendOutputRequest = dispatcher.captureRequest {
            method?.contains("PUT") ?: false
                && path == "/$outputPath"
                && getHeader("Content-Type")?.startsWith("application/json") ?: false
        }

        action(suppressErrors = false).send(
            buildOutput = BuildOutput().apply {
                this.artifacts = stubArtifacts
                this.testResults.putAll(stubTestResults)
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

    @Test
    fun `cd build result - failed if error`() {
        dispatcher.mockResponse(
            { true },
            MockResponse().setResponseCode(500)
        )

        val error = assertThrows(RuntimeException::class.java) {
            action(suppressErrors = false).send(
                buildOutput = BuildOutput().apply {
                    this.artifacts = stubArtifacts
                    this.testResults.putAll(stubTestResults)
                },
                cdBuildConfig = cdBuildConfig,
                versionCode = versionCode,
                teamcityUrl = teamcityUrl,
                gitState = gitState,
                uiTestConfiguration = uiTestConfiguration
            )
        }
        assertThat(error).hasMessageThat().contains("Upload build result")
    }

    @Test
    fun `cd build result - success if suppress error`() {
        dispatcher.mockResponse(
            { true },
            MockResponse().setResponseCode(500)
        )

        action(suppressErrors = true).send(
            buildOutput = BuildOutput().apply {
                this.artifacts = stubArtifacts
                this.testResults.putAll(stubTestResults)
            },
            cdBuildConfig = cdBuildConfig,
            versionCode = versionCode,
            teamcityUrl = teamcityUrl,
            gitState = gitState,
            uiTestConfiguration = uiTestConfiguration
        )
    }

    private fun action(suppressErrors: Boolean) = UploadCdBuildResultTaskAction(
        gson = Providers.gson,
        client = Providers.client(
            user = user,
            password = password
        ),
        suppressErrors = suppressErrors
    )

    private val teamcityUrl = "xxx/viewLog.html?buildId=100&tab=buildLog"
    private val schemaVersion: Long = 2
    private val releaseVersion = "249.0"
    private val versionCode = "100"
    private val gitBranch = CdBuildResult.GitBranch(
        name = "branchName",
        commitHash = "commitHash"
    )
    private val gitState = GitStateStub(
        currentBranch = Branch(
            gitBranch.name,
            gitBranch.commitHash
        )
    )
    private val uiTestConfiguration = "regression"
    private val stubTestResults = mapOf(
        uiTestConfiguration to CdBuildResult.TestResultsLink(
            reportId = "123",
            reportUrl = "no matter url",
            reportCoordinates = CdBuildResult.TestResultsLink.ReportCoordinates(
                planSlug = "2",
                jobSlug = "3",
                runId = "4"
            )
        )
    )
    private val stubArtifacts = listOf(
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
    private val cdBuildConfig = CdBuildConfig(
        schemaVersion = schemaVersion,
        project = NupokatiProject.AVITO,
        releaseVersion = releaseVersion,
        outputDescriptor = CdBuildConfig.OutputDescriptor("${mockWebServer.url("")}$outputPath", true),
        deployments = emptyList()
    )
}
