package com.avito.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.MockWebServerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class SignServicePluginTest {

    private val mockWebServer = MockWebServerFactory.create()

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `plugin apply - success - tasks configuration`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            signServiceExtension = configureExtension()
        )

        ciRun(
            testProjectDir,
            "tasks",
            "-PsignToken=12345",
            dryRun = false,
        ).assertThat().buildSuccessful()
    }

    @Test
    fun `plugin apply - fails - configuration without url`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            signServiceExtension = ""
        )

        ciRun(
            testProjectDir,
            "tasks",
            dryRun = false,
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("Invalid signer url value: ''")
    }

    private fun generateTestProject(
        testProjectDir: File,
        signServiceExtension: String = configureExtension(),
    ) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            buildGradleExtra = """
                |gradleLogger {
                |   printlnHandler(false, com.avito.logger.LogLevel.DEBUG)
                |}
            """.trimMargin(),
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    versionCode = 100,
                    versionName = "22.1",
                    plugins = plugins {
                        id("com.avito.android.signer")
                    },
                    buildGradleExtra = signServiceExtension
                )
            ),
        ).generateIn(testProjectDir)
    }

    private fun configureExtension(
        enabled: Boolean = true,
        url: String = "${mockWebServer.url("/")}"
    ) = """
         signService {
            enabled = $enabled
            url = "$url"
            apk(android.buildTypes.release, project.properties.get("signToken"))
            bundle(android.buildTypes.release, project.properties.get("signToken"))
         }
    """.trimIndent()
}
