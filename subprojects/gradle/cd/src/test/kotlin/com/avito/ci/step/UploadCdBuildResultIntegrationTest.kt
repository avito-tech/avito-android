package com.avito.ci.step

import com.avito.android.plugin.artifactory.setFakeMavenMetadataBody
import com.avito.cd.BuildVariant
import com.avito.cd.CdBuildResult
import com.avito.cd.Providers
import com.avito.cd.uploadCdBuildResultTaskName
import com.avito.ci.bodyContains
import com.avito.git.Git
import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.file
import com.avito.test.http.MockDispatcher
import okhttp3.Credentials
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class UploadCdBuildResultIntegrationTest {

    private lateinit var projectDir: File
    private val server = MockWebServer()
    private val mockUrl = server.url("").toString().removeSuffix("/")
    private val uiTestConfigurationName = "regress"
    private val reportId = "123"
    private val versionName = "11"
    private val versionCode = "12"
    private val artifactoryUser = "deployer"
    private val artifactoryPassword = "deployer_password"
    private val reportsApiUrl = "https://reports"
    private val dispatcher = MockDispatcher(defaultResponse = MockResponse().setResponseCode(200))
        .also { dispatcher -> server.dispatcher = dispatcher }

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
        mockingReportApi()
        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    versionCode = versionCode,
                    versionName = versionName,
                    name = "app",
                    plugins = listOf(
                        "com.avito.android.signer",
                        "com.avito.android.instrumentation-tests",
                        "com.avito.android.artifactory-app-backup",
                        "com.avito.android.cd"
                    ),
                    customScript = """
                            import com.avito.cd.BuildVariant
                            ${registerUiTestConfigurations("regress")}
                            signService {
                                bundle(android.buildTypes.release, "no_matter")
                                host("https://signer/")
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
            )
        ).generateIn(projectDir)
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
            commitHash = Git.Impl(projectDir).tryParseRev("HEAD").get()
        )

        val runId = "runId"
        val uiTestConfiguration = CdBuildResult.TestResults(
            reportId = reportId,
            reportUrl = "$reportsApiUrl/report/AvitoAndroid/regress/$runId",
            reportCoordinates = CdBuildResult.TestResults.ReportCoordinates(
                planSlug = "AvitoAndroid",
                jobSlug = uiTestConfigurationName,
                runId = runId
            )
        )
        val artifacts = listOf(
            CdBuildResult.Artifact.AndroidBinary(
                "apk",
                "$nupokatiProject-11-12-100-releaseApk.apk",
                "$mockUrl/apps-release-local/app-android/$nupokatiProject/11-12-100/$nupokatiProject-11-12-100-releaseApk.apk",
                BuildVariant.RELEASE
            )
        )

        val expected = CdBuildResult(
            schemaVersion = schemaVersion,
            teamcityBuildUrl = "xxx/viewLog.html?buildId=100&tab=buildLog",
            releaseVersion = releaseVersion,
            buildNumber = versionCode,
            testResults = uiTestConfiguration,
            artifacts = artifacts,
            gitBranch = gitBranch
        )

        val configFile = projectDir.file(configFileName)
        configFile.writeText(cdBuildConfig)
        projectDir.file("/app/build/outputs/apk/release/app-release.apk").createNewFile()
        projectDir.file("/app/build/reports/mapping.txt").writeText("1")

        val cdBuildResultRequest = dispatcher.captureRequest {
            method?.contains("PUT") ?: false
                && path == "/$outputPath"
                && getHeader("Content-Type")?.startsWith("application/json") ?: false
        }

        dispatcher.mockResponse(
            requestMatcher = { path?.contains("maven-metadata.xml") ?: false },
            response = MockResponse().setResponseCode(200).setFakeMavenMetadataBody()
        )

        val result = ciRun(
            projectDir,
            ":app:release",
            "-Pcd.build.config.file=$configFileName",
            "-PartifactoryUrl=$mockUrl",
            "-Partifactory_deployer=$artifactoryUser",
            "-Partifactory_deployer_password=$artifactoryPassword",
            "-PdeviceName=LOCAL",
            "-PteamcityBuildId=100",
            "-Papp.versionName=$versionName",
            "-Papp.versionCode=$versionCode",
            "-Pavito.bitbucket.url=http://bitbucket",
            "-Pavito.bitbucket.projectKey=AA",
            "-Pavito.bitbucket.repositorySlug=android",
            "-Pavito.fileStorage.url=http://file-storage",
            "-Pavito.stats.enabled=false",
            "-Pavito.stats.host=http://stats",
            "-Pavito.stats.fallbackHost=http://stats",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=android",
            "-PkubernetesToken=stub",
            "-PkubernetesUrl=stub",
            "-PkubernetesCaCertData=stub",
            branch = gitBranch.name,
            dryRun = false
        )

        result.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":app:$uploadCdBuildResultTaskName")

        cdBuildResultRequest
            .checks
            .singleRequestCaptured()
            .bodyContains(Providers.gson.toJson(expected))
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
            commitHash = Git.Impl(projectDir).tryParseRev("HEAD").get()
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
        projectDir.file("/app/build/outputs/apk/release/app-release.apk").createNewFile()
        projectDir.file("/app/build/reports/mapping.txt").writeText("1")

        dispatcher.mockResponse(
            requestMatcher = { path?.contains("maven-metadata.xml") ?: false },
            response = MockResponse().setResponseCode(200).setFakeMavenMetadataBody()
        )

        val result = ciRun(
            projectDir,
            ":app:release",
            "-Pcd.build.config.file=$configFileName",
            "-PartifactoryUrl=$mockUrl",
            "-Partifactory_deployer=$artifactoryUser",
            "-Partifactory_deployer_password=$artifactoryPassword",
            "-PdeviceName=LOCAL",
            "-PteamcityBuildId=100",
            "-Papp.versionName=$versionName",
            "-Papp.versionCode=$versionCode",
            "-Pavito.bitbucket.url=http://bitbucket",
            "-Pavito.bitbucket.projectKey=AA",
            "-Pavito.bitbucket.repositorySlug=android",
            "-Pavito.fileStorage.url=http://file-storage",
            "-Pavito.stats.enabled=false",
            "-Pavito.stats.host=http://stats",
            "-Pavito.stats.fallbackHost=http://stats",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=android",
            "-PkubernetesToken=stub",
            "-PkubernetesUrl=stub",
            "-PkubernetesCaCertData=stub",
            branch = gitBranch.name,
            dryRun = false
        )

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:$uploadCdBuildResultTaskName", TaskOutcome.SKIPPED)
    }

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

    private fun mockingReportApi() {
        dispatcher.mockResponse(
            requestMatcher = { bodyContains(""""method":"Run.Create"""") },
            response = MockResponse().setBody("""{"result": {"id": "$reportId"}}""")
        )
        dispatcher.mockResponse(
            requestMatcher = { bodyContains(""""method":"Run.GetByParams"""") },
            response = MockResponse().setBody(runGetParamsResponse)
        )
        dispatcher.mockResponse(
            requestMatcher = { bodyContains(""""method":"RunTest.List"""") },
            response = MockResponse().setBody("""{"result": []}""")
        )
    }

    private fun registerUiTestConfigurations(vararg names: String): String {
        val configurations = names.map { name ->
            """$name {
                    reportFlakyTests = false
                    targets {
                        api22 {
                            deviceName = "api22"

                            scheduling {
                                quota {
                                    minimumSuccessCount = 1
                                }

                                staticDevicesReservation {
                                    device = Emulator22.INSTANCE
                                    count = 1
                                }
                            }
                        }
                    }
                }
                """
        }
        return """
            import static com.avito.instrumentation.reservation.request.Device.Emulator.Emulator22

            android.defaultConfig {
                testInstrumentationRunner = "no_matter"
                testInstrumentationRunnerArguments(["planSlug" : "AvitoAndroid"])
            }
            instrumentation {
                 reportViewerUrl="$reportsApiUrl"
                 reportApiFallbackUrl = "stub"
                 sentryDsn = "stub"
                 slackToken = "stub"
                 fileStorageUrl = "stub"
                 registry = "stub"
                 reportApiUrl = "$mockUrl"
                 instrumentationParams = [
                    "deviceName"    : "regress",
                    "jobSlug"       : "regress",
                    "runId"         : "runId"
                ]

                output = "./output"

                configurations {
                    $configurations
                }
            }
        """
    }
}
