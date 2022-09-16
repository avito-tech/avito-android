package com.avito.plugin

import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class QAppsPluginIntegrationTest {

    private val mockWebServer = MockWebServerFactory.create()

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    private data class TestCase(
        val releaseBuildVariants: List<String>,
        val releaseChain: Boolean,
        val expectedReleaseChain: Boolean
    )

    @TestFactory
    fun uploadApkIntegration(@TempDir tempPath: File): Collection<DynamicTest> {
        return listOf(
            TestCase(
                releaseBuildVariants = listOf("debug"),
                releaseChain = false,
                expectedReleaseChain = false,
            ),
            TestCase(
                releaseBuildVariants = listOf("debug"),
                releaseChain = true,
                expectedReleaseChain = true,
            ),
            TestCase(
                releaseBuildVariants = emptyList(),
                releaseChain = true,
                expectedReleaseChain = false,
            ),
            TestCase(
                releaseBuildVariants = emptyList(),
                releaseChain = false,
                expectedReleaseChain = false,
            ),
        ).flatMap { testCase ->
            prepareProject(tempPath, testCase)

            val runResult = runUpload(tempPath)

            val testCollection = mutableListOf<DynamicTest>()

            testCollection += dynamicTest("build is successful") {
                runResult.assertThat().buildSuccessful()
            }

            val request = mockWebServer.takeRequest()

            testCollection += dynamicTest("request path is correct") {
                assertThat(request.path).isEqualTo("/qapps/api/os/android/upload")
            }

            val body = request.body.readUtf8()

            testCollection += listOf(
                "branch" to "My-feature-branch",
                "comment" to "my awesome apk",
                "version_code" to "111",
                "version_name" to "12.3",
                "package_name" to "com.qapps.app",
                "release_chain" to testCase.expectedReleaseChain.toString()
            )
                .map { (key, value) ->
                    dynamicTest("param $key passed with correct value") {
                        assertThat(body).containsMatch(multipartBodyFormData(key, value))
                    }
                }

            testCollection
        }
    }

    private fun multipartBodyFormData(key: String, value: String) =
        Regex("Content-Disposition: form-data; name=\"$key\"[\\s]+Content-Length: [0-9]+[\\s]+$value")
            .toPattern()

    private fun runUpload(projectDir: File): TestResult {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK))

        return gradlew(
            projectDir,
            "-Pavito.build=local",
            "-Pavito.git.state=local",
            ":app:qappsUploadUnsignedDebug",
            expectFailure = false
        )
    }

    private fun prepareProject(
        projectDir: File,
        testCase: QAppsPluginIntegrationTest.TestCase,
    ) {
        val releaseBuildVariants = testCase.releaseBuildVariants
            .joinToString(separator = ",") {
                "\"$it\""
            }

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    imports = listOf(
                        "import com.avito.plugin.qappsUploadUnsignedTaskProvider",
                        "import com.avito.plugin.QAppsUploadTask"
                    ),
                    plugins = plugins {
                        id("com.avito.android.qapps")
                    },
                    versionCode = 111,
                    versionName = "12.3",
                    packageName = "com.qapps.app",
                    buildGradleExtra = """
                         qapps {
                            serviceUrl.set("${mockWebServer.url("/")}")
                            branchName.set("My-feature-branch")
                            comment.set("my awesome apk")
                            releaseBuildVariants.set(listOf($releaseBuildVariants))
                         }
                         tasks.withType<QAppsUploadTask>().configureEach {
                            releaseChain.set(${testCase.releaseChain})
                         }
                         afterEvaluate {
                            tasks.qappsUploadUnsignedTaskProvider("debug").configure {
                                apkDirectory.set(file("${'$'}buildDir/outputs/apk/debug"))
                            }
                         }
                    """.trimIndent(),
                    useKts = true
                )
            )
        ).generateIn(projectDir)

        val apkDirectory = File(projectDir, "app/build/outputs/apk/debug")
        apkDirectory.mkdirs()

        val apk = File(apkDirectory, "app-debug.apk")
        apk.createNewFile()
        apk.writeText("stub apk content")
    }
}
