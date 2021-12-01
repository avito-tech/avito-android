package com.avito.ci.steps

import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.dir
import com.avito.test.gradle.git
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.MockWebServerFactory
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class SignerIntegrationTest {

    private lateinit var projectDir: File

    private val webServer = MockWebServerFactory.create()

    private val syncBranch = "develop"

    @Suppress("MaxLineLength")
    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        webServer.start()
        projectDir = tempPath.toFile()

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.signer")
                        id("com.avito.android.cd")
                    },
                    imports = listOf(
                        "import com.avito.cd.BuildVariant"
                    ),
                    buildGradleExtra = """
                signService {
                    url.set("${webServer.url("/")}")
                    apk(android.buildTypes.release, '12345')
                }
                builds {
                    fullCheck {
                        artifacts {
                            apk("releaseApk",BuildVariant.RELEASE,"", "${'$'}{project.buildDir}/outputs/apk/release/app-release.apk") { 
                                signature = "12321e1e12e1"
                            }
                        }
                    }
                }
            """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        with(projectDir) {
            git("checkout -b $syncBranch")
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

        webServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(HttpCodes.INTERNAL_ERROR)
            }
        }

        val result = fullCheck(expectFailure = true)
        result.assertThat().run {
            taskWithOutcome(":app:legacySignApkViaServiceRelease", TaskOutcome.FAILED)
        }
    }

    private fun fullCheck(expectFailure: Boolean = true): TestResult =
        ciRun(projectDir, "app:fullCheck", "--info", expectFailure = expectFailure)
}
