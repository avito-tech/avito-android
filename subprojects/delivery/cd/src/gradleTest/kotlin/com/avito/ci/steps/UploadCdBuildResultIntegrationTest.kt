package com.avito.ci.steps

import com.avito.android.plugin.artifactory.setStubMavenMetadataBody
import com.avito.cd.BuildVariant
import com.avito.cd.CdBuildResult
import com.avito.cd.uploadCdBuildResultTaskName
import com.avito.cd.uploadCdGson
import com.avito.ci.runTask
import com.avito.git.Git
import com.avito.http.HttpCodes
import com.avito.instrumentation.instrumentationPluginId
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import okhttp3.Credentials
import okhttp3.mockwebserver.MockResponse
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class UploadCdBuildResultIntegrationTest {

    private lateinit var projectDir: File
    private val server = MockWebServerFactory.create()
    private val mockUrl = server.url("").toString().removeSuffix("/")
    private val uiTestConfigurationName = "regress"
    private val reportId = "123"
    private val versionName = "11"
    private val versionCode = 12
    private val artifactoryUser = "deployer"
    private val artifactoryPassword = "deployer_password"
    private val reportsApiUrl = "https://reports"
    private val dispatcher = MockDispatcher(
        unmockedResponse = MockResponse().setResponseCode(200),
    )
        .also { dispatcher -> server.dispatcher = dispatcher }

    private val runGetParamsResponse =
        """
            {
                "result": {
                    "id": "$reportId",
                    "planSlug": "AvitoAndroid",
                    "jobSlug": "$uiTestConfigurationName",
                    "runId": "xxx",
                    "isFinished": true
                }
            }
        """.trimIndent()

    @Suppress("MaxLineLength")
    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()

        mockingReportApi()
        mockFileStorageApi()

        val androidAppModule = AndroidAppModule(
            versionCode = versionCode,
            versionName = versionName,
            name = "app",
            plugins = plugins {
                id("com.avito.android.signer")
                id(instrumentationPluginId)
                id("com.avito.android.artifactory-app-backup")
                id("com.avito.android.cd")
                id("maven-publish")
            },
            imports = listOf(
                "import com.avito.cd.BuildVariant"
            ),
            buildGradleExtra = """
                        ${registerUiTestConfigurations("regress")}
                        signService {
                            url.set("https://signer/")
                            bundle(android.buildTypes.release, "no_matter")
                        }
                        builds {
                            release {
                                uiTests { configurations "$uiTestConfigurationName" }

                                artifacts {
                                    apk("releaseApk", BuildVariant.RELEASE, "com.app", "${'$'}{project.buildDir}/outputs/apk/release/app-release-unsigned.apk") {}
                                    mapping("releaseMapping", BuildVariant.RELEASE, "${'$'}{project.buildDir}/reports/mapping.txt")
                                }

                                uploadToArtifactory {
                                    artifacts = ['releaseApk']
                                }
                                uploadBuildResult {
                                    uiTestConfiguration = "$uiTestConfigurationName"
                                }
                            }
                        }
                        """.trimIndent()
        )

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(androidAppModule)
        ).generateIn(projectDir)

        with(projectDir) {
            dir("${androidAppModule.name}/src/androidTest/kotlin/test") {
                kotlinClass("RealTest") {
                    """
package ${androidAppModule.packageName}

class RealTest {
    
    @org.junit.Test
    fun test() {
    }
}
                                """.trimIndent()
                }
            }
        }
    }

    @Test
    fun `upload cd build result - success send in integration`() {
        val configFileName = "xxx"
        val outputPath = "path"
        val nupokatiProject = "avito"
        val schemaVersion: Long = 2
        val releaseVersion = "249.0"
        val cdBuildConfig = """
        {
            "schema_version": $schemaVersion,
            "project": "$nupokatiProject",
            "release_version": "$releaseVersion",
            "output_descriptor": {
                "path": "$mockUrl/$outputPath",
                "skip_upload": false
            },
            "deployments": [
                {
                    "type": "google-play",
                    "artifact_type": "bundle",
                    "build_variant": "release",
                    "track": "alpha"
                }
            ]
        }
        """
        val gitBranch = CdBuildResult.GitBranch(
            name = "branchName",
            commitHash = Git.create(
                rootDir = projectDir,
            ).tryParseRev("HEAD").getOrThrow()
        )

        val commit = projectDir.git("rev-parse HEAD").trim()

        val runId = "$commit.teamcity-BT"
        val uiTestConfiguration = CdBuildResult.TestResultsLink(
            reportUrl = "$reportsApiUrl/report/AvitoAndroid/regress/$runId?q=eyJmaWx0ZXIiOnsic2tpcCI6MH19",
            reportCoordinates = CdBuildResult.TestResultsLink.ReportCoordinates(
                planSlug = "AvitoAndroid",
                jobSlug = uiTestConfigurationName,
                runId = runId
            )
        )
        val artifacts = listOf(
            CdBuildResult.Artifact.AndroidBinary(
                "apk",
                "$nupokatiProject-11-12-100-releaseApk.apk",
                "$mockUrl/apps-release-local/app-android/" +
                    "$nupokatiProject/11-12-100/$nupokatiProject-11-12-100-releaseApk.apk",
                BuildVariant.RELEASE
            )
        )

        val expected = CdBuildResult(
            schemaVersion = schemaVersion,
            teamcityBuildUrl = "xxx/viewLog.html?buildId=100&tab=buildLog",
            releaseVersion = releaseVersion,
            buildNumber = versionCode.toString(),
            testResults = uiTestConfiguration,
            artifacts = artifacts,
            gitBranch = gitBranch
        )

        val configFile = projectDir.file(configFileName)
        configFile.writeText(cdBuildConfig)
        projectDir.file("/app/build/reports/mapping.txt").writeText("1")

        val cdBuildResultRequest = dispatcher.captureRequest {
            recordedRequest.method?.contains("PUT") ?: false
                && path == "/$outputPath"
                && recordedRequest.getHeader("Content-Type")?.startsWith("application/json") ?: false
        }

        dispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("maven-metadata.xml") },
                response = MockResponse().setResponseCode(HttpCodes.OK).setStubMavenMetadataBody()
            )
        )

        val result = runTask(
            projectDir,
            ":app:release",
            "-Pavito.git.state=env",
            "-Pcd.build.config.file=$configFileName",
            "-PartifactoryUrl=$mockUrl",
            "-Partifactory_deployer=$artifactoryUser",
            "-Partifactory_deployer_password=$artifactoryPassword",
            branch = gitBranch.name,
            dryRun = false
        )

        result.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":app:$uploadCdBuildResultTaskName")

        cdBuildResultRequest
            .checks
            .singleRequestCaptured()
            .bodyContains(uploadCdGson.toJson(expected))
            .containsHeader("Authorization", Credentials.basic(artifactoryUser, artifactoryPassword))
    }

    @Test
    fun `upload cd build result - sending task skipped`() {
        val configFileName = "xxx"
        val outputPath = "path"
        val schemaVersion: Long = 2
        val releaseVersion = "249.0"
        val gitBranch = CdBuildResult.GitBranch(
            name = "branchName",
            commitHash = Git.create(
                rootDir = projectDir,
            ).tryParseRev("HEAD").getOrThrow()
        )
        val cdBuildConfig = """
        {
            "schema_version": $schemaVersion,
            "project": "avito_test",
            "release_version": "$releaseVersion",
            "output_descriptor": {
                "path": "$mockUrl/$outputPath",
                "skip_upload": true
            },
            "deployments": [
                {
                    "type": "google-play",
                    "artifact_type": "bundle",
                    "build_variant": "release",
                    "track": "alpha"
                }
            ]
        }
        """

        val configFile = projectDir.file(configFileName)
        configFile.writeText(cdBuildConfig)
        projectDir.file("/app/build/reports/mapping.txt").writeText("1")

        dispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("maven-metadata.xml") },
                response = MockResponse().setResponseCode(HttpCodes.OK).setStubMavenMetadataBody()
            )
        )

        val result = runTask(
            projectDir,
            ":app:release",
            "-Pcd.build.config.file=$configFileName",
            "-PartifactoryUrl=$mockUrl",
            "-Partifactory_deployer=$artifactoryUser",
            "-Partifactory_deployer_password=$artifactoryPassword",
            branch = gitBranch.name,
            dryRun = false
        )

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:$uploadCdBuildResultTaskName", TaskOutcome.SKIPPED)
    }

    private fun mockFileStorageApi() {
        dispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("file/addBinary") },
                response = MockResponse().setBody("file/uploaded")
            )
        )
    }

    private fun mockingReportApi() {
        dispatcher.registerMock(
            Mock(
                requestMatcher = { bodyContains(""""method":"Run.Create"""") },
                response = MockResponse().setBody("""{"result": {"id": "$reportId"}}""")
            )
        )
        dispatcher.registerMock(
            Mock(
                requestMatcher = { bodyContains(""""method":"Run.GetByParams"""") },
                response = MockResponse().setBody(runGetParamsResponse)
            )
        )

        dispatcher.registerMock(
            Mock(
                requestMatcher = { bodyContains(""""method":"Run.SetFinished"""") },
                response = MockResponse().setBody("""{"result": "ok!"}""")
            )
        )

        dispatcher.registerMock(
            Mock(
                requestMatcher = { bodyContains(""""method":"RunTest.List"""") },
                response = MockResponse().setBody(
                    """{
    "result": [
        {
            "id": "21w12e21e1e12e12r12",
            "test_name": "com.app.RealTest::test",
            "status": 1,
            "environment": "api22",
            "attempts_count": 1,
            "success_count": 1
        }
    ]
}"""
                )
            )
        )
        dispatcher.registerMock(
            Mock(
                requestMatcher = { bodyContains(""""method":"RunTest.AddFull"""") },
                response = MockResponse().setBody("""{"result": "ok!"}""")
            )
        )
    }

    private fun registerUiTestConfigurations(vararg names: String): String {
        val configurations = names.map { name ->
            """$name {
                    targets {
                        api22 {
                            deviceName = "api22"

                            scheduling {
                                quota {
                                    minimumSuccessCount = 1
                                }

                                staticDevicesReservation {
                                    device = MockEmulator.create(22)
                                    count = 1
                                }
                            }
                        }
                    }
                }
                """
        }
        return """
            import static com.avito.instrumentation.reservation.request.Device.MockEmulator

            android.defaultConfig {
                testInstrumentationRunner = "no_matter"
                testInstrumentationRunnerArguments(["planSlug" : "AvitoAndroid"])
            }
            instrumentation {
                 
                 instrumentationParams = [
                    "deviceName"    : "regress",
                    "jobSlug"       : "regress",
                ]
                
                testReport {
                    reportViewer {
                        reportApiUrl = "$mockUrl"
                        reportViewerUrl = "$reportsApiUrl"
                        fileStorageUrl = "$mockUrl"
                    }
                }

                output = rootProject.file("outputs").path

                configurations {
                    $configurations
                }
            }
        """
    }
}
