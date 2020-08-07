package com.avito.ci.step

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.http.MockWebServerFactory
import okhttp3.mockwebserver.MockResponse
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class UploadToQappsTest {

    private lateinit var projectDir: File
    private val mockWebServer = MockWebServerFactory.create()

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
    }

    @Test
    fun `upload to qapps - no cd build config`() {
        generateProject()

        gradlew(
            projectDir, "app:release",
            "-Pci=true", "-Pavito.build=local", "-Pavito.git.state=local"
        ).assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:qappsUploadDebug", TaskOutcome.SUCCESS)
    }

    @Test
    fun `upload to qapps - cd build config with qapps deployment`() {
        generateProject()
        val configFile = createCdConfig(
            """
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
                },
                {
                    "type": "qapps"
                }
            ]
        }
        """
        )

        gradlew(
            projectDir, "app:release",
            "-Pci=true", "-Pavito.build=local", "-Pavito.git.state=local", "-Pcd.build.config.file=$configFile"
        ).assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:qappsUploadDebug", TaskOutcome.SUCCESS)
    }

    @Test
    fun `skip upload to qapps - cd build config without qapps deployment`() {
        generateProject()
        val configFile = createCdConfig(
            """
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
            ]
        }
        """
        )

        gradlew(
            projectDir, "app:release",
            "-Pci=true", "-Pavito.build=local", "-Pavito.git.state=local", "-Pcd.build.config.file=$configFile"
        ).assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:qappsUploadDebug", TaskOutcome.SKIPPED)
    }

    private fun generateProject() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = listOf(
                        "com.avito.android.qapps",
                        "com.avito.android.signer",
                        "com.avito.android.cd"
                    ),
                    customScript = """
                            qapps {
                                serviceUrl = "${mockWebServer.url("/")}"
                                branchName = "develop"
                                comment = "build #1"
                            }
                            signService {
                                host = "stub"
                                // no keys to skip a task
                            }
                            builds {
                                release {
                                    useImpactAnalysis = false
                                    artifacts {
                                        apk("debug", com.avito.cd.BuildVariant.DEBUG, "com.app", "${'$'}buildDir/outputs/apk/debug/app-debug.apk") {}
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

    private fun createCdConfig(config: String): String {
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
