package com.avito.android.plugin.artifactory

import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

private typealias Artifact = Pair<String, String>

internal class ArtifactoryAppBackupPluginTest {

    private val mockWebServer = MockWebServerFactory.create()

    @Test
    fun `artifactory plugin - failed to apply - without maven-publish plugin`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    enableKotlinAndroidPlugin = false,
                    versionCode = 90,
                    versionName = "10",
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.artifactory-app-backup")
                    },
                    imports = listOf(
                        "import static com.avito.android.plugin.artifactory.ArtifactoryAppBackupInterfaceKt." +
                            "getArtifactoryAndroidArtifactsBuildVariants",
                        "import com.avito.cd.BuildVariant",
                    ),
                    buildGradleExtra = """
                        $artifactoryBackupExtensionName {
                            backup {
                                name = "name"
                                type = "type"
                                version = "version"
                                artifact {
                                    id = "classifier"
                                    path = "artifact"
                                }
                            }
                        }
                        
                        getArtifactoryAndroidArtifactsBuildVariants(project).put("classifier", BuildVariant.STAGING)
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        val result = ciRun(
            projectDir,
            ":app:$artifactoryAppBackupTaskName",
            "-PartifactoryUrl=${mockWebServer.url("/")}",
            "-Partifactory_deployer=xxx",
            "-Partifactory_deployer_password=xxx",
            expectFailure = true
        )

        result.assertThat()
            .buildFailed()
            .outputContains("artifactory-app-backup has precondition: maven-publish plugin should be applied")
    }

    @Test
    fun `artifactory plugin - captures app parameters`(@TempDir projectDir: File) {
        val moduleName = "app"
        val backupName = "backupName"
        val backupType = "backupType"
        val backupVersion = "backupVersion"
        val classifier = "releaseApk"
        val artifactName = "xxx.apk"
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    enableKotlinAndroidPlugin = false,
                    versionCode = 90,
                    versionName = "10",
                    name = moduleName,
                    plugins = plugins {
                        id("com.avito.android.artifactory-app-backup")
                        id("maven-publish")
                    },
                    imports = listOf(
                        "import static com.avito.android.plugin.artifactory.ArtifactoryAppBackupInterfaceKt." +
                            "getArtifactoryAndroidArtifactsBuildVariants",
                        "import com.avito.cd.BuildVariant"
                    ),
                    buildGradleExtra = """
                        $artifactoryBackupExtensionName {
                            backup {
                                name = "$backupName"
                                type = "$backupType"
                                version = "$backupVersion"
                                artifact {
                                    id = "$classifier"
                                    path = "$artifactName"
                                }
                            }
                        }
                        
                        getArtifactoryAndroidArtifactsBuildVariants(project).put("$classifier", BuildVariant.STAGING)
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        Files.createFile(Paths.get(projectDir.path, moduleName, artifactName))

        val dispatcher = MockDispatcher(
            unmockedResponse = MockResponse().setResponseCode(HttpCodes.OK),
        )
            .also { mockWebServer.dispatcher = it }

        dispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("maven-metadata.xml") },
                response = MockResponse().setResponseCode(HttpCodes.OK).setStubMavenMetadataBody()
            )
        )

        val rootPomRequest = dispatcher.captureRequest { path.endsWith(".pom") }
        val putApkRequest =
            dispatcher.captureRequest { path.endsWith(".apk") && method.lowercase() == "put" }

        val result = ciRun(
            projectDir,
            ":$moduleName:$artifactoryAppBackupTaskName",
            "-PartifactoryUrl=${mockWebServer.url("/")}",
            "-Partifactory_deployer=xxx",
            "-Partifactory_deployer_password=xxx"
        )

        result
            .assertThat()
            .buildSuccessful()

        rootPomRequest.checks.singleRequestCaptured().apply {
            pathContains("$backupName/$backupType/$backupVersion/$backupType-$backupVersion")
        }

        putApkRequest.checks.singleRequestCaptured().apply {
            pathContains("$backupName/$backupType/$backupVersion/$backupType-$backupVersion-$classifier.apk")
        }
    }

    @Test
    fun `upload to artifactory - success - multiple files with same extension`(@TempDir projectDir: File) {
        val moduleName = "app"
        val backupName = "backupName"
        val backupType = "backupType"
        val backupVersion = "backupVersion"
        val artifacts = setOf<Artifact>(
            "id1" to "xxx1.json",
            "id2" to "xxx2.json"
        )
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    enableKotlinAndroidPlugin = false,
                    versionCode = 90,
                    versionName = "10",
                    name = moduleName,
                    plugins = plugins {
                        id("com.avito.android.artifactory-app-backup")
                        id("maven-publish")
                    },
                    buildGradleExtra = """
                        $artifactoryBackupExtensionName {
                            backup {
                                name = "$backupName"
                                type = "$backupType"
                                version = "$backupVersion"
                                ${
                        artifacts.map { (id, path) ->
                            """
                                artifact {
                                    id = "$id"
                                    path = "$path"
                                }    
                            """.trimIndent()
                        }.joinToString(separator = "\n")
                    }
                            }
                        }
                    """.trimIndent(),
                )
            )
        ).generateIn(projectDir)

        artifacts.forEach { (_, path) ->
            Files.createFile(Paths.get(projectDir.path, moduleName, path))
        }

        val dispatcher = MockDispatcher(
            unmockedResponse = MockResponse().setResponseCode(HttpCodes.OK),
        )
            .also { mockWebServer.dispatcher = it }

        dispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("maven-metadata.xml") },
                response = MockResponse().setResponseCode(HttpCodes.OK).setStubMavenMetadataBody()
            )
        )

        val rootPomRequest = dispatcher.captureRequest { path.endsWith(".pom") }
        val putJsonFileRequests =
            artifacts.map { (id, _) ->
                dispatcher.captureRequest {
                    path.endsWith("$id.json") && method.lowercase() == "put"
                }
            }

        val result = ciRun(
            projectDir,
            ":$moduleName:$artifactoryAppBackupTaskName",
            "-PartifactoryUrl=${mockWebServer.url("/")}",
            "-Partifactory_deployer=xxx",
            "-Partifactory_deployer_password=xxx"
        )

        result
            .assertThat()
            .buildSuccessful()

        rootPomRequest.checks.singleRequestCaptured().apply {
            pathContains("$backupName/$backupType/$backupVersion/$backupType-$backupVersion")
        }

        artifacts.forEachIndexed { index, (id, _) ->
            putJsonFileRequests[index].checks.singleRequestCaptured()
                .pathContains("$backupName/$backupType/$backupVersion/$backupType-$backupVersion-$id.json")
        }
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }
}
