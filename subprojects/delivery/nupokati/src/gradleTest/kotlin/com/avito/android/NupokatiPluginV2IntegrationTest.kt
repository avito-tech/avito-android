package com.avito.android

import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.git
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import okhttp3.mockwebserver.MockResponse
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class NupokatiPluginV2IntegrationTest {

    private val mockDispatcher = MockDispatcher()

    init {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    method == "PUT"
                        && path == "/artifactory/mobile-releases/avito_android/118.0_2/app-release.aab"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK)
            )
        )

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    method == "POST"
                        && path == "/qapps/api/os/android/upload"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK)
            )
        )
    }

    private val mockWebServer = MockWebServerFactory.create().apply {
        dispatcher = mockDispatcher
    }

    private val releaseVersion = "118.0"

    private val outputDescriptorPath =
        mockWebServer.url("/artifactory/mobile-releases/avito_android/118.0_2/release_info.json")

    private val mockWebServerUrl = mockWebServer.url("/")

    private val reportViewerFrontendUrl = "http://stub.com"
    private val planSlug = "AvitoAndroid"
    private val jobSlug = "FunctionalTest"
    private val runId = "someId"
    private val teamcityUrl = "http://teamcity.ru"
    private val versionCode = 122

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun `uploadCdBuildResult - json contains required data`(@TempDir projectDir: File) {
        val cdConfig = """
            |{
            |  "schema_version": 2,
            |  "project": "avito",
            |  "release_version": "$releaseVersion",
            |  "output_descriptor": {
            |    "path": "$outputDescriptorPath",
            |    "skip_upload": false
            |  },
            |  "deployments": [
            |    {
            |      "type": "qapps",
            |      "is_release": true
            |    }
            |  ]
            |}""".trimMargin()

        val cdConfigFile = File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }

        generateProject(cdConfigFile, projectDir)

        val branchName = "release_11"
        projectDir.git("checkout -b $branchName")
        val commit = projectDir.git("rev-parse HEAD").trim()

        val contractJson = mockDispatcher.captureRequest(
            Mock(
                requestMatcher = {
                    method == "PUT" && path == "/artifactory/mobile-releases/avito_android/118.0_2/release_info.json"
                },
                response = MockResponse().setResponseCode(200)
            )
        )

        val nupokatiTaskResult = gradlew(
            projectDir,
            ":app:nupokati",
            dryRun = false
        )

        nupokatiTaskResult
            .assertThat()
            .buildSuccessful()

        nupokatiTaskResult
            .assertThat()
            .tasksShouldBeTriggered(
                ":app:qappsUploadUnsignedRelease",
                ":app:artifactoryBackupRelease",
                ":app:uploadCdBuildResultRelease"
            )

        val expectedContract = """
        |{
        |  "schema_version": 2,
        |  "teamcity_build_url": "$teamcityUrl",
        |  "build_number": "$versionCode",
        |  "release_version": "$releaseVersion",
        |  "git_branch": {
        |    "name": "$branchName",
        |    "commit_hash": "$commit"
        |  },
        |  "test_results": {
        |    "report_url": "$reportViewerFrontendUrl/report/$planSlug/$jobSlug/$runId?q=eyJmaWx0ZXIiOnsic2tpcCI6MH19",
        |    "report_coordinates": {
        |      "plan_slug": "$planSlug",
        |      "job_slug": "$jobSlug",
        |      "run_id": "$runId"
        |    }
        |  },
        |  "artifacts": [
        |    {
        |      "type": "bundle",
        |      "name": "app-release.aab",
        |      "uri": "${mockWebServerUrl}artifactory/mobile-releases/avito_android/118.0_2/app-release.aab",
        |      "build_variant": "release"
        |    }
        |  ]
        |}
        |""".trimMargin()

        contractJson.checks.singleRequestCaptured().jsonEquals(expectedContract)
    }

    @Test
    fun `no qapps deployment - qapps task skipped`(@TempDir projectDir: File) {
        val cdConfig = """
            |{
            |  "schema_version": 2,
            |  "project": "avito",
            |  "release_version": "$releaseVersion",
            |  "output_descriptor": {
            |    "path": "$outputDescriptorPath",
            |    "skip_upload": false
            |  },
            |  "deployments": []
            |}""".trimMargin()

        val cdConfigFile = File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }

        generateProject(cdConfigFile, projectDir)

        val branchName = "release_11"
        projectDir.git("checkout -b $branchName")
        val commit = projectDir.git("rev-parse HEAD").trim()

        val contractJson = mockDispatcher.captureRequest(
            Mock(
                requestMatcher = {
                    method == "PUT" && path == "/artifactory/mobile-releases/avito_android/118.0_2/release_info.json"
                },
                response = MockResponse().setResponseCode(200)
            )
        )

        val nupokatiTaskResult = gradlew(
            projectDir,
            ":app:nupokati",
            dryRun = false
        )

        nupokatiTaskResult
            .assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:qappsUploadUnsignedRelease", TaskOutcome.SKIPPED)
            .tasksShouldBeTriggered(
                ":app:artifactoryBackupRelease",
                ":app:uploadCdBuildResultRelease"
            )

        val expectedContract = """
        |{
        |  "schema_version": 2,
        |  "teamcity_build_url": "$teamcityUrl",
        |  "build_number": "$versionCode",
        |  "release_version": "$releaseVersion",
        |  "git_branch": {
        |    "name": "$branchName",
        |    "commit_hash": "$commit"
        |  },
        |  "test_results": {
        |    "report_url": "$reportViewerFrontendUrl/report/$planSlug/$jobSlug/$runId?q=eyJmaWx0ZXIiOnsic2tpcCI6MH19",
        |    "report_coordinates": {
        |      "plan_slug": "$planSlug",
        |      "job_slug": "$jobSlug",
        |      "run_id": "$runId"
        |    }
        |  },
        |  "artifacts": [
        |    {
        |      "type": "bundle",
        |      "name": "app-release.aab",
        |      "uri": "${mockWebServerUrl}artifactory/mobile-releases/avito_android/118.0_2/app-release.aab",
        |      "build_variant": "release"
        |    }
        |  ]
        |}
        |""".trimMargin()

        contractJson.checks.singleRequestCaptured().jsonEquals(expectedContract)
    }

    private fun generateProject(cdConfigFile: File, projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.qapps")
                        id("com.avito.android.nupokati")
                    },
                    versionCode = versionCode,
                    useKts = true,
                    imports = listOf(
                        "import com.avito.reportviewer.model.ReportCoordinates",
                        "import com.avito.plugin.qappsUploadUnsignedTaskProvider",
                    ),
                    buildGradleExtra = """
                            |android {
                            |    buildTypes {
                            |        getByName("release") {
                            |            isMinifyEnabled = true
                            |            proguardFile("proguard.pro")
                            |        }
                            |    }
                            |}
                            |
                            |qapps {
                            |   branchName.set("develop")
                            |   comment.set("stub comment")
                            |   serviceUrl.set("${mockWebServer.url("/")}")
                            |}
                            |
                            |nupokati {
                            |    cdBuildConfigFile.set(rootProject.file("${cdConfigFile.name}"))
                            |    teamcityBuildUrl.set("$teamcityUrl")
                            |
                            |    artifactory {
                            |        login.set("user")
                            |        password.set("12345")
                            |    }
                            |
                            |    reportViewer {
                            |        frontendUrl.set("$reportViewerFrontendUrl")
                            |        reportCoordinates.set(ReportCoordinates("$planSlug", "$jobSlug", "$runId"))
                            |    }
                            |}
                            |
                            |tasks.named("nupokati") {
                            |   dependsOn(tasks.qappsUploadUnsignedTaskProvider("release"))
                            |}
                            |""".trimMargin()
                )
            )
        ).generateIn(projectDir)
    }
}
