package com.avito.android

import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.git
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.avito.test.http.RequestData
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
            useKts = true,
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.tls-configuration")
            },
            buildGradleExtra = """
                ${createMtlsExtensionString()}
            """.trimIndent(),
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
                        "import com.avito.android.model.input.config.parser.CdBuildConfigParser",
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
                        |    val config = CdBuildConfigParser.parseCdBuildConfigV4(
                        |        rootProject.file("${cdConfigFile.path}")
                        |    )
                        |
                        |    v4("releaseV4") {
                        |        config?.let {
                        |            cdBuildConfig.set(it)
                        |        }
                        |        versionCode.set($versionCode)
                        |        useTls.set(false)
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
                        |               ArtifactV4.AppBinary(
                        |                   storeName = null,
                        |                   file = layout.buildDirectory.file("test.apk").map { it.asFile },
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

    private fun MockDispatcher.registerSequencedResponses(
        matcher: RequestData.() -> Boolean,
        vararg responses: MockResponse,
    ) {
        responses.reversed().forEach { response ->
            registerMock(
                Mock(
                    requestMatcher = matcher,
                    response = response,
                    removeAfterMatched = true,
                )
            )
        }
    }

    @Test
    fun `nupokati v4 - uploadArtifact - retries on 503 and succeeds`(@TempDir projectDir: File) {
        val cdConfigFile = writeCdConfig(projectDir)
        generateProject(cdConfigFile, projectDir)
        projectDir.git("checkout -b release_11")

        val uploadCapturer = mockDispatcher.captureRequest {
            method == "POST" && path == "/api/1/upload_artifact"
        }
        mockDispatcher.registerSequencedResponses(
            matcher = { method == "POST" && path == "/api/1/upload_artifact" },
            MockResponse().setResponseCode(503),
            MockResponse().setResponseCode(503),
            MockResponse().setResponseCode(HttpCodes.OK).setBody(
                """
                {"buildNumber":$versionCode,"version":"$releaseVersion","platform":"android",
                "project":"avito","uri":"${mockWebServerUrl}artifacts/app-release.aab"}
                """.trimIndent()
            ),
        )
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { method == "POST" && path == "/saveTestResult/" },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(
                    """{"result":{"success":true,"message":"ok"}}"""
                ),
            )
        )

        gradlew(projectDir, ":app:nupokati", dryRun = false)
            .assertThat().buildSuccessful()

        uploadCapturer.checks.requestsCaptured(requestsCount = 3)
    }

    @Test
    fun `nupokati v4 - uploadArtifact - fails after retries exhausted`(@TempDir projectDir: File) {
        val cdConfigFile = writeCdConfig(projectDir)
        generateProject(cdConfigFile, projectDir)
        projectDir.git("checkout -b release_11")

        val uploadCapturer = mockDispatcher.captureRequest {
            method == "POST" && path == "/api/1/upload_artifact"
        }
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { method == "POST" && path == "/api/1/upload_artifact" },
                response = MockResponse().setResponseCode(503).setBody("persistent"),
            )
        )

        gradlew(projectDir, ":app:nupokati", expectFailure = true, dryRun = false)
            .assertThat()
            .buildFailed()
            .taskWithOutcome(":app:uploadNupokatiArtifactsReleaseV4", TaskOutcome.FAILED)

        uploadCapturer.checks.requestsCaptured(requestsCount = 3)
    }

    @Test
    fun `nupokati v4 - chunked upload - retries on part and succeeds`(@TempDir projectDir: File) {
        val cdConfigFile = writeCdConfig(projectDir)
        generateProject(cdConfigFile, projectDir, chunkedUploadThresholdBytes = 10L)
        projectDir.git("checkout -b release_11")

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { method == "POST" && path == "/multipartUploadInit/" },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(
                    """{"result":{"uploadId":"u1"}}"""
                ),
            )
        )
        val partCapturer = mockDispatcher.captureRequest {
            method == "POST" && path == "/api/1/upload_part_artifact"
        }
        mockDispatcher.registerSequencedResponses(
            matcher = { method == "POST" && path == "/api/1/upload_part_artifact" },
            MockResponse().setResponseCode(503),
            MockResponse().setResponseCode(HttpCodes.OK).setBody(
                """{"etag":"e-1","partNumber":1}"""
            ),
        )
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { method == "POST" && path == "/multipartUploadComplete/" },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(
                    """{"result":{"uri":"${mockWebServerUrl}artifacts/app-release.aab"}}"""
                ),
            )
        )
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { method == "POST" && path == "/saveTestResult/" },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(
                    """{"result":{"success":true,"message":"ok"}}"""
                ),
            )
        )

        gradlew(projectDir, ":app:nupokati", dryRun = false)
            .assertThat().buildSuccessful()

        // part was retried once: 2 captured requests for the part endpoint
        partCapturer.checks.requestsCaptured(requestsCount = 2)
    }

    @Test
    fun `nupokati v4 - saveTestResult - retries on 503`(@TempDir projectDir: File) {
        val cdConfigFile = writeCdConfig(projectDir)
        generateProject(cdConfigFile, projectDir)
        projectDir.git("checkout -b release_11")

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { method == "POST" && path == "/api/1/upload_artifact" },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(
                    """
                    {"buildNumber":$versionCode,"version":"$releaseVersion","platform":"android",
                    "project":"avito","uri":"${mockWebServerUrl}artifacts/app-release.aab"}
                    """.trimIndent()
                ),
            )
        )
        val saveCapturer = mockDispatcher.captureRequest {
            method == "POST" && path == "/saveTestResult/"
        }
        mockDispatcher.registerSequencedResponses(
            matcher = { method == "POST" && path == "/saveTestResult/" },
            MockResponse().setResponseCode(503),
            MockResponse().setResponseCode(HttpCodes.OK).setBody(
                """{"result":{"success":true,"message":"ok"}}"""
            ),
        )

        gradlew(projectDir, ":app:nupokati", dryRun = false)
            .assertThat().buildSuccessful()

        saveCapturer.checks.requestsCaptured(requestsCount = 2)
    }

    @Test
    fun `nupokati v4 - uploadArtifact - does not retry on 4xx`(@TempDir projectDir: File) {
        val cdConfigFile = writeCdConfig(projectDir)
        generateProject(cdConfigFile, projectDir)
        projectDir.git("checkout -b release_11")

        val uploadCapturer = mockDispatcher.captureRequest {
            method == "POST" && path == "/api/1/upload_artifact"
        }
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { method == "POST" && path == "/api/1/upload_artifact" },
                response = MockResponse().setResponseCode(400).setBody("bad request"),
            )
        )

        gradlew(projectDir, ":app:nupokati", expectFailure = true, dryRun = false)
            .assertThat().buildFailed()

        uploadCapturer.checks.requestsCaptured(requestsCount = 1)
    }

    private fun writeCdConfig(projectDir: File): File {
        val cdConfig = """
            |{
            |  "schema_version": 4,
            |  "project": "avito",
            |  "release_version": "$releaseVersion",
            |  "skip_upload": false
            |}""".trimMargin()
        return File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }
    }

    private fun generateProject(
        cdConfigFile: File,
        projectDir: File,
        chunkedUploadThresholdBytes: Long? = null,
    ) {
        TestProjectGenerator(
            useKts = true,
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.tls-configuration")
            },
            buildGradleExtra = """
                ${createMtlsExtensionString()}
            """.trimIndent(),
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
                        "import com.avito.android.model.input.config.parser.CdBuildConfigParser",
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
                        |    val config = CdBuildConfigParser.parseCdBuildConfigV4(
                        |        rootProject.file("${cdConfigFile.path}")
                        |    )
                        |
                        |    v4("releaseV4") {
                        |        config?.let {
                        |            cdBuildConfig.set(it)
                        |        }
                        |        versionCode.set($versionCode)
                        |        useTls.set(false)
                        |        nupokatiUrl.set("$mockWebServerUrl")
                        |        ${chunkedUploadThresholdBytes?.let { "chunkedUploadThresholdBytes.set(${it}L)" } ?: ""}
                        |        artifacts.set(
                        |           listOf(
                        |               ArtifactV4.AppBinary(
                        |                   storeName = null,
                        |                   file = layout.buildDirectory.file("test.apk").map { it.asFile },
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
