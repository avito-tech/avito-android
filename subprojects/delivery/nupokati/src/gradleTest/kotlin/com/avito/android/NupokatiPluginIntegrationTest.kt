package com.avito.android

import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.google.common.net.HttpHeaders
import okhttp3.mockwebserver.MockResponse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class NupokatiPluginIntegrationTest {

    private val mockDispatcher = MockDispatcher()

    private val mockWebServer = MockWebServerFactory.create().apply {
        dispatcher = mockDispatcher
    }

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun `uploadCdBuildResult - json contains required data`(@TempDir projectDir: File) {

        val outputDescriptorPath =
            mockWebServer.url("/artifactory/mobile-releases/avito_android/118.0_2/release_info.json")

        @Language("json")
        val cdConfig = """
            |{
            |  "schema_version": 2,
            |  "project": "avito",
            |  "release_version": "118.0",
            |  "output_descriptor": {
            |    "path": "$outputDescriptorPath",
            |    "skip_upload": false
            |  },
            |  "deployments": [
            |    {
            |      "type": "google-play",
            |      "artifact_type": "bundle",
            |      "build_variant": "release",
            |      "track": "beta"
            |    },
            |    {
            |      "type": "qapps",
            |      "is_release": true
            |    }
            |  ]
            |}""".trimMargin()

        val cdConfigFile = File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }

        val keyFile = File(projectDir, "keyfile.json").also { it.writeText("SECRET") }

        val mockWebServerUrl = mockWebServer.url("/")

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.nupokati")
                    },
                    useKts = true,
                    imports = listOf("import com.avito.reportviewer.model.ReportCoordinates"),
                    buildGradleExtra = """
                        |android {
                        |   buildTypes {
                        |       getByName("release") {
                        |           isMinifyEnabled = true
                        |           proguardFile("proguard.pro")
                        |       }
                        |   }
                        |}
                        |
                        |nupokati {
                        |   cdBuildConfigFile.set(rootProject.file("${cdConfigFile.name}"))
                        |   uploadCrashlyticsMapping.set(false)
                        |   suppressFailures.set(true)
                        |   teamcityBuildUrl.set("http://teamcity.ru")
                        |   
                        |   artifactory {
                        |       login.set("user")
                        |       password.set("12345")
                        |   }
                        |   
                        |   googlePlay {
                        |       keyFile.set(rootProject.file("${keyFile.name}"))
                        |       mockUrl.set("$mockWebServerUrl")
                        |   }
                        |   
                        |   reportViewer {
                        |       frontendUrl.set("http://stub.com")
                        |       reportCoordinates.set(ReportCoordinates("1", "2", "3"))
                        |   }
                        |}
                        |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    method == "PUT"
                        && path == "/artifactory/mobile-releases/avito_android/118.0_2/app-release.aab"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK)
            )
        )

        mockGooglePlay()

        gradlew(
            projectDir,
            ":app:uploadCdBuildResultRelease",
            dryRun = false
        )
            .assertThat()
            .buildSuccessful()

        // todo assert requests
    }

    private fun mockGooglePlay() {
        val editId = "12345"

        @Language("json")
        val editsResponse = """
        |{
        |  "id": "$editId"
        |}
        """.trimMargin()

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    method == "POST"
                        && path == "/androidpublisher/v3/applications/com.app/edits"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(editsResponse)
            )
        )

        @Language("json")
        val uploadResponse = """
        |{
        |  "versionCode": 123
        |}
        """.trimMargin()

        val uploadPath = "/upload/androidpublisher/v3/applications/com.app/edits/$editId/bundles?uploadType=resumable"

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    (method == "POST" || method == "PUT") && path == uploadPath
                },
                response = MockResponse()
                    .setResponseCode(HttpCodes.OK)
                    .setBody(uploadResponse)
                    .setHeader(
                        HttpHeaders.LOCATION,
                        mockWebServer.url(uploadPath)
                    )
            )
        )

        @Language("json")
        val setTrack = """
        |{
        |}
        """.trimMargin()

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    method == "PUT" &&
                        path == "/androidpublisher/v3/applications/com.app/edits/$editId/tracks/beta"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(setTrack)
            )
        )

        @Language("json")
        val commitAllResponse = """
        |{
        |}
        """.trimMargin()

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    method == "POST" &&
                        path == "/androidpublisher/v3/applications/com.app/edits/$editId:commit"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK).setBody(commitAllResponse)
            )
        )
    }
}
