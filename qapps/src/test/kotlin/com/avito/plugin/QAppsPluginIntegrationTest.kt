package com.avito.plugin

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class QAppsPluginIntegrationTest {

    private lateinit var projectDir: File

    private val mockWebServer = MockWebServer()

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = listOf("com.avito.android.qapps"),
                    buildGradleExtra = """
                         qapps {
                            host = "${mockWebServer.url("/")}"
                         }
                         afterEvaluate {
                            qappsUploadDebug {
                                apk = file("${'$'}buildDir/outputs/apk/debug/app-debug.apk")
                            }
                         }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        val apkDirectory = File(projectDir, "app/build/outputs/apk/debug")
        apkDirectory.mkdirs()

        val apk = File(apkDirectory, "app-debug.apk")
        apk.createNewFile()
        apk.writeText("fake apk content")
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `send apk - qapps upload task`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val result = gradlew(":app:assembleDebug", ":app:qappsUploadDebug", expectFailure = false)

        result.assertThat().buildSuccessful()
        assertThat(mockWebServer.requestCount).isEqualTo(1)
        val request = mockWebServer.takeRequest()
        assertThat(request.path).isEqualTo("/qapps/api/os/android/upload")
    }

    private fun gradlew(vararg args: String, expectFailure: Boolean = false): TestResult =
        com.avito.test.gradle.gradlew(projectDir, *args, expectFailure = expectFailure)
}
