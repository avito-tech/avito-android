package com.avito.ci

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.dir
import com.avito.test.gradle.git
import com.avito.test.gradle.kotlinClass
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class SignerIntegrationTest {

    private lateinit var projectDir: File

    private val webServer = MockWebServer()

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        webServer.start()
        projectDir = tempPath.toFile()

        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = listOf(
                        "com.avito.android.signer",
                        "com.avito.android.cd"
                    ),
                    buildGradleExtra = """
                        import com.avito.cd.BuildVariant
                signService {
                    apk(android.buildTypes.release, '12345')
                    bundle(android.buildTypes.release, '12345')
                    host = "${webServer.url("/")}"
                }
                builds {
                    fullCheck {
                        artifacts {
                            apk("releaseApk",BuildVariant.RELEASE,"", "${'$'}{project.buildDir}/outputs/apk/release/app-release.apk") { }
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

    @AfterEach
    fun teardown() {
        webServer.shutdown()
    }

    @Test
    fun `fullCheck - failed if sign via service is failed`() {
        with(projectDir) {
            git("checkout -b newBranch")
            dir("appA/src/main/kotlin") {
                kotlinClass("NewClass")
            }
            git("add .")
            git("commit -m 'new'")
        }

        webServer.setDispatcher(object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(500)
            }
        })

        val result = fullCheck(expectFailure = true)
        result.assertThat().run {
            taskWithOutcome(":app:signApkViaServiceRelease", TaskOutcome.FAILED)
        }
    }

    private fun fullCheck(expectFailure: Boolean = true): TestResult =
        ciRun(projectDir, "app:fullCheck", "--info", expectFailure = expectFailure)
}

private const val SYNC_BRANCH = "develop"
