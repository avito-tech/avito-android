package com.avito.cd

import com.avito.git.Branch
import com.avito.git.GitStateStub
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.Credentials
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UploadCdBuildResultTaskActionTest {

    private val mockWebServer = MockWebServerFactory.create()
    private val dispatcher = MockDispatcher()
    private val gson = uploadCdGson
    private val user = "user"
    private val password = "password"
    private val outputPath = "file_path"
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

    private val testResults = CdBuildResult.TestResultsLink(
        reportUrl = "no matter url",
        reportCoordinates = CdBuildResult.TestResultsLink.ReportCoordinates(
            planSlug = "2",
            jobSlug = "3",
            runId = "4"
        )
    )

    private val stubTestResults = mapOf(uiTestConfiguration to testResults)

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
        dispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setResponseCode(200)
            )
        )

        val sendOutputRequest = dispatcher.captureRequest {
            method.contains("PUT")
                && path == "/$outputPath"
                && recordedRequest.getHeader("Content-Type")?.startsWith("application/json") ?: false
        }

        action(suppressErrors = false).send(
            buildOutput = BuildOutput().apply {
                this.artifacts = stubArtifacts
            },
            cdBuildConfig = cdBuildConfig,
            versionCode = versionCode,
            teamcityUrl = teamcityUrl,
            gitState = gitState,
            testResults = testResults
        )

        sendOutputRequest
            .checks
            .singleRequestCaptured()
            .bodyContains(gson.toJson(expected))
            .containsHeader("Authorization", Credentials.basic(user, password))
    }

    @Test
    fun `cd build result - failed if error`() {
        dispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setResponseCode(500)
            )
        )

        val error = assertThrows(RuntimeException::class.java) {
            action(suppressErrors = false).send(
                buildOutput = BuildOutput().apply {
                    this.artifacts = stubArtifacts
                },
                cdBuildConfig = cdBuildConfig,
                versionCode = versionCode,
                teamcityUrl = teamcityUrl,
                gitState = gitState,
                testResults = testResults
            )
        }
        assertThat(error).hasMessageThat().contains("Upload build result")
    }

    @Test
    fun `cd build result - success if suppress error`() {
        dispatcher.registerMock(
            Mock(
                requestMatcher = { true },
                response = MockResponse().setResponseCode(500)
            )
        )

        action(suppressErrors = true).send(
            buildOutput = BuildOutput().apply {
                this.artifacts = stubArtifacts
            },
            cdBuildConfig = cdBuildConfig,
            versionCode = versionCode,
            teamcityUrl = teamcityUrl,
            gitState = gitState,
            testResults = testResults
        )
    }

    private fun action(suppressErrors: Boolean) = UploadCdBuildResultTaskAction(
        gson = gson,
        client = Providers.client(
            user = user,
            password = password
        ),
        suppressErrors = suppressErrors
    )
}
