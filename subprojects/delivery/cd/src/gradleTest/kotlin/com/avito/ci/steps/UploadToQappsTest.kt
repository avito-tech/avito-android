package com.avito.ci.steps

import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.MockWebServerFactory
import okhttp3.mockwebserver.MockResponse
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class UploadToQappsTest {

    private lateinit var projectDir: File
    private val mockWebServer = MockWebServerFactory.create()

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK))
    }

    @Test
    fun `upload to qapps - no cd build config`() {
        generateProject()

        gradlew(
            projectDir,
            "app:release",
            "-Pci=true",
            "-Pavito.build=local",
            "-Pavito.git.state=local"
        ).assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:qappsUploadDebug", TaskOutcome.SUCCESS)
    }

    @Test
    fun `upload to qapps - cd build config with qapps deployment`() {
        generateProject()
        val configFile = createCdConfig(withQapps = true)

        gradlew(
            projectDir,
            "app:release",
            "-Pci=true",
            "-Pavito.build=local",
            "-Pavito.git.state=local",
            "-Pcd.build.config.file=$configFile"
        ).assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:qappsUploadDebug", TaskOutcome.SUCCESS)
    }

    @Test
    fun `skip upload to qapps - cd build config without qapps deployment`() {
        generateProject()
        val configFile = createCdConfig(withQapps = false)

        gradlew(
            projectDir,
            "app:release",
            "-Pci=true",
            "-Pavito.build=local",
            "-Pavito.git.state=local",
            "-Pcd.build.config.file=$configFile"
        ).assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:qappsUploadDebug", TaskOutcome.SKIPPED)
    }

    @Suppress("MaxLineLength")
    private fun generateProject() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.qapps")
                        id("com.avito.android.signer")
                        id("com.avito.android.cd")
                    },
                    buildGradleExtra = """
                            qapps {
                                serviceUrl = "${mockWebServer.url("/")}"
                                branchName = "develop"
                                comment = "build #1"
                            }
                            signService {
                                url.set("http://stub")
                                // no keys to skip a task
                            }
                            builds {
                                release {
                                    useImpactAnalysis = false
                                    artifacts {
                                        apk("debug", com.avito.cd.BuildVariant.DEBUG, "com.app.debug", "${'$'}buildDir/outputs/apk/debug/app-debug.apk") {}
                                    }
                                    uploadToQapps {
                                        artifacts = ["debug"]
                                    }
                                }
                            }
                        """.trimIndent()
                )
            )
        ).generateIn(projectDir)
    }

    private fun createCdConfig(withQapps: Boolean): String {
        val config = """
        {
            "schema_version": 2,
            "release_version": "1.0",
            "output_descriptor": {
                "path": "http://foo.bar",
                "skip_upload": true
            },
            "deployments": [
                {
                    "type": "google-play",
                    "artifact_type": "apk",
                    "build_variant": "debug",
                    "track": "alpha"
                }
                ${if (withQapps) ", { \"type\": \"qapps\" }" else ""}
            ]
        }
        """

        val fileName = "cd_config.json"
        val file = projectDir.file(fileName)
        file.createNewFile()
        file.writeText(config)
        return fileName
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }
}
