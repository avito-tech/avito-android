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

internal class NupokatiPluginV4IntegrationTest {

    private val mockDispatcher = MockDispatcher()

    private val mockWebServer = MockWebServerFactory.create().apply {
        dispatcher = mockDispatcher
    }

    private val releaseVersion = "118.0"
    private val mockWebServerUrl = mockWebServer.url("/")
    private val reportViewerFrontendUrl = "https://rv.example.com"
    private val planSlug = "AvitoAndroid"
    private val jobSlug = "FunctionalTest"
    private val runId = "someId"
    private val teamcityUrl = "https://tc.exmple.com"
    private val versionCode = 122

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun `nupokati v4 - uploads artifact and sends test results`(@TempDir projectDir: File) {
        val cdConfig = """
            |{
            |  "schema_version": 4,
            |  "project": "avito",
            |  "release_version": "$releaseVersion",
            |  "skip_upload": false
            |}""".trimMargin()

        val cdConfigFile = File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }

        generateProject(cdConfigFile, projectDir)

        val branchName = "release_11"
        projectDir.git("checkout -b $branchName")

        val uploadArtifactRequest = mockDispatcher.captureRequest(
            Mock(
                requestMatcher = {
                    method == "POST" && path == "/api/1/upload_artifact"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(
                    """
                    {
                        "buildNumber": $versionCode,
                        "version": "$releaseVersion",
                        "platform": "android",
                        "project": "avito",
                        "uri": "${mockWebServerUrl}artifacts/app-release.aab"
                    }
                    """.trimIndent()
                )
            )
        )

        val saveTestResultRequest = mockDispatcher.captureRequest(
            Mock(
                requestMatcher = {
                    method == "POST" && path == "/saveTestResult/"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(
                    """
                    {
                        "result": {
                            "success": true,
                            "message": "Test result saved successfully"
                        }
                    }
                    """.trimIndent()
                )
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
                ":app:uploadNupokatiArtifactsReleaseV4",
                ":app:sendTestResultsReleaseV4"
            )

        uploadArtifactRequest.checks.singleRequestCaptured()

        saveTestResultRequest.checks.singleRequestCaptured()
            .bodyContains("\"project\":\"avito\"")
            .bodyContains("\"platform\":\"android\"")
            .bodyContains("\"buildNumber\":$versionCode")
            .bodyContains(
                "\"reportUrl\":" +
                    "\"$reportViewerFrontendUrl/report/$planSlug/$jobSlug/$runId?q=eyJmaWx0ZXIiOnsic2tpcCI6MH19\""
            )
            .bodyContains("\"planSlug\":\"$planSlug\"")
            .bodyContains("\"jobSlug\":\"$jobSlug\"")
            .bodyContains("\"runId\":\"$runId\"")
    }

    @Test
    fun `nupokati v4 - fails when artifact upload fails`(@TempDir projectDir: File) {
        val cdConfig = """
            |{
            |  "schema_version": 4,
            |  "project": "avito",
            |  "release_version": "$releaseVersion",
            |  "skip_upload": false
            |}""".trimMargin()

        val cdConfigFile = File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }

        generateProject(cdConfigFile, projectDir)

        val branchName = "release_11"
        projectDir.git("checkout -b $branchName")

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    method == "POST" && path == "/api/1/upload_artifact"
                },
                response = MockResponse().setResponseCode(500).setBody(
                    "Upload failed"
                )
            )
        )

        val nupokatiTaskResult = gradlew(
            projectDir,
            ":app:nupokati",
            expectFailure = true,
            dryRun = false
        )

        nupokatiTaskResult
            .assertThat()
            .buildFailed()
            .taskWithOutcome(":app:uploadNupokatiArtifactsReleaseV4", TaskOutcome.FAILED)
    }

    @Test
    fun `nupokati v4 - idempotent configuration - calling v4 twice with same name works`(@TempDir projectDir: File) {
        val cdConfig = """
            |{
            |  "schema_version": 4,
            |  "project": "avito",
            |  "release_version": "$releaseVersion",
            |  "skip_upload": false
            |}""".trimMargin()

        val cdConfigFile = File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }

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
                        "import com.avito.android.gradle_configuration.extension.ArtifactV4"
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
                        |    branchName.set("develop")
                        |    comment.set("stub comment")
                        |    serviceUrl.set("$mockWebServerUrl")
                        |}
                        |
                        |nupokati {
                        |    cdBuildConfigFile.set(rootProject.file("${cdConfigFile.name}"))
                        |
                        |    v4("releaseV4") {
                        |        nupokatiUrl.set("$mockWebServerUrl")
                        |
                        |        reportViewer {
                        |            frontendUrl.set("$reportViewerFrontendUrl")
                        |            reportCoordinates.set(ReportCoordinates("$planSlug", "$jobSlug", "$runId"))
                        |        }
                        |    }
                        |
                        |    v4("releaseV4") {
                        |        // Additional configuration
                        |        artifacts.set(
                        |           listOf(
                        |               ArtifactV4.Artifact(
                        |                   file = provider {
                        |                       layout.buildDirectory.file("test.apk").get().asFile.toPath() 
                        |                   }
                        |               )
                        |           )
                        |        )
                        |    }
                        |}
                        |
                        |tasks.register("nupokati") {
                        |   dependsOn("nupokatiReleaseV4")
                        |}
                        |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        // Should not fail with duplicate pipeline name
        gradlew(projectDir, "tasks").assertThat().buildSuccessful()
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
                        "import com.avito.android.gradle_configuration.extension.ArtifactV4"
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
                        |    branchName.set("develop")
                        |    comment.set("stub comment")
                        |    serviceUrl.set("$mockWebServerUrl")
                        |}
                        |
                        |nupokati {
                        |    cdBuildConfigFile.set(rootProject.file("${cdConfigFile.name}"))
                        |
                        |    v4("releaseV4") {
                        |        nupokatiUrl.set("$mockWebServerUrl")
                        |
                        |        artifacts.set(
                        |           listOf(
                        |               ArtifactV4.Artifact(
                        |                   file = provider {
                        |                       layout.buildDirectory.file("test.apk").get().asFile.toPath() 
                        |                   }
                        |               )
                        |           )
                        |        )
                        |
                        |        reportViewer {
                        |            frontendUrl.set("$reportViewerFrontendUrl")
                        |            reportCoordinates.set(ReportCoordinates("$planSlug", "$jobSlug", "$runId"))
                        |        }
                        |    }
                        |}
                        |
                        |tasks.register("nupokati") {
                        |   dependsOn("nupokatiReleaseV4")
                        |}
                        |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        File("$projectDir/app/build/test.apk").apply {
            parentFile.mkdirs()
            writeText("test apk content")
        }
    }
}
