package com.avito.ci.steps

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.git
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class PerformanceIntegrationTest {

    private lateinit var projectDir: File

    private val performanceConfigurationName = "somePerfTests"

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()

        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = listOf(
                        "com.avito.android.instrumentation-tests",
                        "com.avito.android.performance",
                        "com.avito.android.cd"
                    ),
                    customScript = """
                         ${registerUiTestConfigurations(performanceConfigurationName)}

                        performance {
                            output = project.rootProject.file("outputs/app/performance_tests/").path
                            performanceTestResultName = "performance_tests.json"
                            statsUrl = "someUrl"
                        }

                        builds {
                            fullCheck {
                                performanceTests {
                                    configuration = "$performanceConfigurationName"
                                }
                            }
                        }
            """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        with(projectDir) {
            git("checkout -b $SYNC_BRANCH")
        }
    }

    private fun registerUiTestConfigurations(vararg names: String): String {
        val configurations = names.map { name ->
            """$name {
                
                    performanceType = com.avito.instrumentation.configuration.InstrumentationConfiguration.PerformanceType.SIMPLE
                    
                    targets {
                        api22 {
                            deviceName = "api22"

                            scheduling {
                                quota {
                                    minimumSuccessCount = 1
                                }

                                staticDevicesReservation {
                                    device = LocalEmulator.device(27)
                                    count = 1
                                }
                            }
                        }
                    }
                }
                """
        }
        return """
            import static com.avito.instrumentation.reservation.request.Device.LocalEmulator

            android.defaultConfig {
                testInstrumentationRunner = "no_matter"
                testInstrumentationRunnerArguments(["planSlug" : "AvitoAndroid"])
            }
            instrumentation {
            reportApiUrl = "stub"
             reportApiFallbackUrl = "stub"
             reportViewerUrl = "stub"
             registry = "stub"
             sentryDsn = "stub"
             slackToken = "stub"
             fileStorageUrl = "stub"
                instrumentationParams = [
                    "deviceName"    : "regress",
                    "jobSlug"       : "regress"
                ]

                output = "/"

                configurations {
                    $configurations
                }
            }
        """
    }

    @Test
    fun `fullCheck - should be altered by measurePerformanceTask`() {
        val result = fullCheck(expectFailure = false)
        result.assertThat().run {
            tasksShouldBeTriggered(":app:measureSomePerfTests")
        }
    }

    private fun fullCheck(expectFailure: Boolean = true): TestResult =
        ciRun(
            projectDir,
            "app:fullCheck",
            "--info",
            "-PdeviceName=LOCAL",
            "-PteamcityBuildId=0",
            "-Papp.versionName=123",
            "-Papp.versionCode=123",
            "-Pavito.bitbucket.url=http://bitbucket",
            "-Pavito.bitbucket.projectKey=AA",
            "-Pavito.bitbucket.repositorySlug=android",
            "-Pavito.stats.enabled=false",
            "-Pavito.stats.host=http://stats",
            "-Pavito.stats.fallbackHost=http://stats",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=android",
            "-Pavito.repo.ssh.url=someUrl",
            "-PkubernetesToken=stub",
            "-PkubernetesUrl=stub",
            "-PkubernetesCaCertData=stub",
            expectFailure = expectFailure,
            dryRun = true,
            targetBranch = SYNC_BRANCH
        )
}

private const val SYNC_BRANCH = "origin/develop"
