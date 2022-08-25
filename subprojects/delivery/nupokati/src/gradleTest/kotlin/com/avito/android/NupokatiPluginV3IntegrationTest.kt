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
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class NupokatiPluginV3IntegrationTest {

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

        val releaseVersion = "118.0"

        @Language("json")
        val cdConfig = """
            |{
            |  "schema_version": 3,
            |  "project": "avito",
            |  "release_version": "$releaseVersion",
            |  "output_descriptor": {
            |    "path": "$outputDescriptorPath",
            |    "skip_upload": false
            |  },
            |  "deployments": [
            |    {
            |      "type": "app-binary",
            |      "store": "ru-store",
            |      "file_type": "apk",
            |      "build_configuration": "release"
            |    },
            |    {
            |      "type": "app-binary",
            |      "store": "google-play",
            |      "file_type": "bundle",
            |      "build_configuration": "release"
            |    },
            |    {
            |      "type": "artifact",
            |      "file_type": "json",
            |      "kind": "feature-toggles"
            |    }
            |  ]
            |}""".trimMargin()

        val cdConfigFile = File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }

        val mockWebServerUrl = mockWebServer.url("/")

        val reportViewerFrontendUrl = "http://stub.com"
        val planSlug = "AvitoAndroid"
        val jobSlug = "FunctionalTest"
        val runId = "someId"
        val teamcityUrl = "http://teamcity.ru"
        val versionCode = 122

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.nupokati")
                    },
                    versionCode = versionCode,
                    useKts = true,
                    imports = listOf(
                        "import com.avito.reportviewer.model.ReportCoordinates",
                        "import com.avito.android.artifactory_backup.ArtifactoryBackupTask",
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
                        |androidComponents {
                        |    val release = selector().withBuildType("release")
                        |    onVariants(release) { variant ->
                        |        tasks.withType<ArtifactoryBackupTask>().configureEach {
                        |            this.artifacts.add(
                        |                variant.artifacts.get(com.android.build.api.artifact.SingleArtifact.BUNDLE)
                        |                .map {
                        |                    com.avito.android.model.input.DeploymentV3.AppBinary(
                        |                        store = "google-play",
                        |                        buildConfiguration = "release",
                        |                        file = it.asFile,
                        |                    )
                        |                }
                        |            )
                        |
                        |            this.artifacts.add(
                        |                variant.artifacts.get(com.android.build.api.artifact.SingleArtifact.APK)
                        |                .map {
                        |                    com.avito.android.model.input.DeploymentV3.AppBinary(
                        |                        store = "ru-store",
                        |                        buildConfiguration = "release",
                        |                        file = it.getApkOrThrow(),
                        |                    )
                        |                }
                        |            )
                        |        }
                        |    }
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
                        |afterEvaluate {
                        |    tasks.withType<ArtifactoryBackupTask>().configureEach {
                        |        this.artifacts.add(
                        |            com.avito.android.model.input.DeploymentV3.Artifact(
                        |                kind = "feature-toggles",
                        |                file = layout.projectDirectory.file("feature_toggles.json").asFile,
                        |            )
                        |        )
                        |    }
                        |}
                        |
                        |fun org.gradle.api.file.Directory.getApkOrThrow(): File {
                        |    val dir = asFile
                        |    val apks = dir.listFiles().orEmpty().filter { it.extension == "apk" }
                        |
                        |    require(apks.size < 2) { "Multiple APK are not supported" }
                        |    return requireNotNull(apks.firstOrNull()) { "APK not found" }
                        |}
                        |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        File("$projectDir/app/feature_toggles.json").writeText("")

        val branchName = "release_11"
        projectDir.git("checkout -b $branchName")
        val commit = projectDir.git("rev-parse HEAD").trim()

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
                    method == "PUT"
                        && path == "/artifactory/mobile-releases/avito_android/118.0_2/app-release-unsigned.apk"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK)
            )
        )

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    method == "PUT"
                        && path == "/artifactory/mobile-releases/avito_android/118.0_2/feature_toggles.json"
                },
                response = MockResponse().setResponseCode(HttpCodes.OK)
            )
        )

        val contractJson = mockDispatcher.captureRequest(
            Mock(
                requestMatcher = {
                    method == "PUT" && path == "/artifactory/mobile-releases/avito_android/118.0_2/release_info.json"
                },
                response = MockResponse().setResponseCode(200)
            )
        )

        gradlew(
            projectDir,
            ":app:uploadCdBuildResultRelease",
            dryRun = false
        )
            .assertThat()
            .buildSuccessful()

        // todo add QAPPS test
        val expectedContract = """
        |{
        |  "schema_version": 3,
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
        |      "store": "ru-store",
        |      "type": "app-binary",
        |      "file_type": "apk",
        |      "name": "app-release-unsigned.apk",
        |      "uri": "${mockWebServerUrl}artifactory/mobile-releases/avito_android/118.0_2/app-release-unsigned.apk",
        |      "build_configuration": "release"
        |    },
        |    {
        |      "store": "google-play",
        |      "type": "app-binary",
        |      "file_type": "bundle",
        |      "name": "app-release.aab",
        |      "uri": "${mockWebServerUrl}artifactory/mobile-releases/avito_android/118.0_2/app-release.aab",
        |      "build_configuration": "release"
        |    },
        |    {
        |      "type": "artifact", 
        |      "file_type": "json",
        |      "name": "feature_toggles.json", 
        |      "uri": "${mockWebServerUrl}artifactory/mobile-releases/avito_android/118.0_2/feature_toggles.json", 
        |      "kind" : "feature-toggles" 
        |    }
        |  ]
        |}
        |""".trimMargin()

        contractJson.checks.singleRequestCaptured().jsonEquals(expectedContract)
    }
}
